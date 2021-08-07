import {CallableContext} from "firebase-functions/lib/providers/https";
import {DataSnapshot} from "firebase-functions/lib/providers/database";

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// Constants
const MIN_PLAYER_NAME = 3;
const AMOUNT_OF_PLAYERS_IN_GAME = 2;

interface Player {
  name: string;
  id: string;
  lastUpdate: number;
}

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
    "lastUpdate": new Date().getTime(),
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
const checkMatchMaking = () : void => {
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
    updates[`games/${gameID}/gameID`] = gameID;
    updates[`games/${gameID}/player${i+1}ID`] = players[i].id;
  }

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
      notification: {
        title: "START_GAME",
        body: "",
      },
    };

    // send notification to players
    functions.logger.info("Send notification to players");
    await admin.messaging().sendToDevice(playerDeviceIDS, payload);
    functions.logger.info("Notification sent to players");
  } catch (error) {
    functions.logger.error("Failed to send notification for players", error);
  }
};