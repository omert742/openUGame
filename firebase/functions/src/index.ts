import {CallableContext} from "firebase-functions/lib/providers/https";
import {DataSnapshot} from "firebase-functions/lib/providers/database";

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// Constants
const MIN_PLAYER_NAME = 3;
const AMOUNT_OF_PLAYERS_IN_GAME = 2;

interface Game {
  currentTurn: number;
  player1: Player;
  player2: Player;
  turns: Turn[];
}

interface Turn {
  winner: string;

  [key: string]: string;
}

interface Player {
  name: string;
  id: string;
  lastUpdate: string;
}

interface MessagePayload {
  data: MessageData;
}

interface MessageData {
  action: string;
  value: string;
}

/**
 * Firebase exported function
 * Called when a player submitted a score and waiting for opponent
 */
exports.checkScore = functions.https.onCall((data: any, context: CallableContext) => {
  const gameID = data?.gameID;
  const turn = data?.turn;

  if (!gameID) {
    throw new functions.https.HttpsError("invalid-argument", "No game id provided");
  }

  if (!turn) {
    throw new functions.https.HttpsError("invalid-argument", "No turn provided");
  }

  return admin.database().ref("/games").child(gameID).once("value").then((snapshot: DataSnapshot) => {
    const dbRef = snapshot.ref;
    const game: Game = <Game>snapshot.toJSON();
    const currentTurn = game.currentTurn;
    const updates: any = {};
    const now = new Date().toISOString();


    const player = findMeInGame(context.instanceIdToken, game);
    updates[`${player}/lastUpdate`] = now;


    const myToken = context.instanceIdToken || "";
    // @ts-ignore
    const opponentToken = game[findOpponentInGame(myToken, game)]?.id;
    let winner = "";

    functions.logger.log(`opponent token:  ${opponentToken}`);
    functions.logger.log(`my token token:  ${myToken}`);
    if (!game.turns?.[turn]) {
      return;// already sent a message regarding a winner,ignore check
    }

    const myResult = new Date(game.turns?.[turn]?.[myToken]);
    const opponentResult = new Date(game.turns?.[turn]?.[opponentToken]);

    functions.logger.log(`game id:  ${gameID} received turn : ${turn} current turn in db  : ${currentTurn}`);

    functions.logger.log(`opponent result:  ${opponentResult}`);
    functions.logger.log(`my result result:  ${myResult}`);

    if (myResult?.getTime?.() > 0 && opponentResult?.getTime?.() > 0) {
      if (myResult.getTime() > opponentResult.getTime()) {
        winner = opponentToken;
      } else {
        winner = myToken;
      }
    }

    if (winner.length > 0 && turn == currentTurn) {
      functions.logger.log(`winner was found:  ${winner}`);
      updates[`turns/${currentTurn}/winner`] = winner;
      updates["currentTurn"] = currentTurn + 1;
    }

    functions.logger.log("updating db values");
    return dbRef.update(updates).then(async () => {
      functions.logger.log("Updated DB values");

      return {
        "winner": winner,
        "turn": currentTurn,
      };
      /*      if (winner) {
        const payload = {
          data: {
            action: "NEXT_TURN",
            value: winner,
          },
        };


        await sendMessageToDevice([game.player1.id, game.player2.id], payload).then(() => {
          functions.logger.log("Message sent to players");
        });
      } else {
        functions.logger.log("waiting for both players result..");
      }
      */
    });
  });
});

/**
 * Firebase exported function
 * Called when a player completed his turn successfully
 */
exports.sendScore = functions.https.onCall((data: any, context: CallableContext) => {
  const gameID = data?.gameID;
  const turn = data?.turn;

  if (!gameID) {
    throw new functions.https.HttpsError("invalid-argument", "No game id provided");
  }
  if (!turn) {
    throw new functions.https.HttpsError("invalid-argument", "No turn provided");
  }

  console.log(`Looking into '${gameID}' with turn ${turn}`);

  admin.database().ref("/games").child(gameID).once("value").then((snapshot: DataSnapshot) => {
    const dbRef = snapshot.ref;
    const game: Game = <Game>snapshot.toJSON();
    const currentTurn = game.currentTurn;

    const updates: any = {};
    const now = new Date().toISOString();
    let playerID: string;
    if (game.player1.id == context.instanceIdToken) {
      functions.logger.log("Updating player 1 last update");
      playerID = game.player1.id;
      updates["player1/lastUpdate"] = now;
    } else if (game.player2.id == context.instanceIdToken) {
      functions.logger.log("Updating player 2 last update");
      playerID = game.player2.id;
      updates["player2/lastUpdate"] = now;
    } else {
      throw new functions.https.HttpsError("invalid-argument", "Failed to find player in game " + gameID);
    }

    if (currentTurn && turn == currentTurn && !game.turns?.[currentTurn]?.[playerID]) {
      functions.logger.log("Updating game values");
      updates[`turns/${currentTurn}/${playerID}`] = now;
    }

    functions.logger.log("updating db values");
    dbRef.update(updates).then(() => {
      functions.logger.log(`Updated game ${gameID} turn ${turn} and player ${playerID} with a score`);
    });
  });
});
/**
 * Firebase exported function
 * when a player wants to start a game he would call this function which would add him to the waiting list
 * and would check if possible to start the game
 */
exports.addPlayerToWaitingList = functions.https.onCall((data: any, context: CallableContext) => {
  functions.logger.log("Started 'addPlayerToWaitingList' function");
  const name: string = data?.name;

  // check for invalid parameters
  if (!name || name.length < MIN_PLAYER_NAME) {
    throw new functions.https.HttpsError("invalid-argument", "Player name is invalid");
  } else if (!context.instanceIdToken) {
    throw new functions.https.HttpsError("invalid-argument", "Unable to get device token");
  }

  // Saving the new message to the Realtime Database.
  return admin.database().ref("/players").child(context.instanceIdToken).set({
    "name": name,
    "id": context.instanceIdToken,
    "lastUpdate": new Date().toISOString(),
  } as Player)
      .then(() => {
        functions.logger.log(`Player '${name}' was added to waiting list`);
        checkMatchMaking();
        return;
      })
      .catch((error: any) => {
      // Re-throwing the error as an HttpsError so that the client gets the error details.
        throw new functions.https.HttpsError("unknown", error.message, error);
      });
});

/**
 * Checks if we can start a game between 2 players
 */
const checkMatchMaking = (): void => {
  functions.logger.log("Checking for match making...");

  admin.database().ref("/players").orderByChild("lastUpdate").limitToFirst(AMOUNT_OF_PLAYERS_IN_GAME).once("value")
      .then((snapshot: DataSnapshot) => {
        functions.logger.log(`Found ${snapshot.numChildren()} results`);
        if (snapshot?.numChildren() == AMOUNT_OF_PLAYERS_IN_GAME) {
          functions.logger.log("Start matchmaking");
          // Create an array of players we are going to do the matchmaking with
          const players: Player[] = [];
          snapshot.forEach((player) => {
            players.push(<Player>player.toJSON());
          });

          startMatchMaking(players);
        } else {
          functions.logger.log("Not enough players waiting to play...");
        }
      });
};

/**
 * Starts the game between 2 players
 * @param {Player[]} players - The players we start the game with
 */
const startMatchMaking = async (players: Player[]) => {
  if (!players || players.length != AMOUNT_OF_PLAYERS_IN_GAME) {
    functions.logger.log("Invalid players array");
    return;
  }
  const updates: any = {};
  const dbRef = admin.database().ref();

  const playerDeviceIDS = players.map((player: Player) => player.id);
  // create game ID
  const gameID = dbRef.child("games").push().getKey();


  for (let i = 0; i < players.length; i++) {
    // Remove players from waiting list
    updates[`players/${players[i].id}`] = null;

    // setup game values
    updates[`games/${gameID}/player${i + 1}`] = players[i];
  }
  updates[`games/${gameID}/currentTurn`] = 1;

  try {
    functions.logger.info("Update database values");
    await dbRef.update(updates);
    functions.logger.info("Database updated successfully");
  } catch (error) {
    functions.logger.error("Failed to send updates", error);
  }

  try {
    // Notification details.
    const payload = {
      data: {
        action: "START_GAME",
        value: gameID,
      },
    };

    // send notification to players
    functions.logger.info("Send notification to players");
    await sendMessageToDevice(playerDeviceIDS, payload);
    functions.logger.info("Notification sent to players");
  } catch (error) {
    functions.logger.error("Failed to send notification for players", error);
  }
};


/**
 * Sends message to given device id's
 * @param {string[]} deviceIDs The devices we want to send message to
 * @param {string} payload The payload we want to send
 */
const sendMessageToDevice = async (deviceIDs: string[], payload: MessagePayload) => {
  deviceIDs.forEach((deviceID: string) => {
    functions.logger.info("sending message to "+ deviceID);
  });
  await admin.messaging().sendToDevice(deviceIDs, payload, {
    timeToLive: 300, // keep message alive only for 5 minute
    priority: "high",
    collapseKey: "openuMessage",
  });
};


const findMeInGame = (playerID: string | undefined, game: Game): string => {
  if (!playerID) {
    throw new functions.https.HttpsError("invalid-argument", "no player id provided");
  }
  switch (playerID) {
    case game.player1.id:
      return "player1";
    case game.player2.id:
      return "player2";
    default:
      throw new functions.https.HttpsError("invalid-argument", "player wasn't found in the game");
  }
};

const findOpponentInGame = (myPlayerID: string | undefined, game: Game): string => {
  if (!myPlayerID) {
    throw new functions.https.HttpsError("invalid-argument", "no player id provided");
  }
  switch (myPlayerID) {
    case game.player1.id:
      return "player2";
    case game.player2.id:
      return "player1";
    default:
      throw new functions.https.HttpsError("invalid-argument", "player wasn't found in the game");
  }
};
