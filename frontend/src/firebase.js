import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { appConfig } from "./config.js";

const firebaseApp = initializeApp(appConfig.firebase);

export const auth = getAuth(firebaseApp);
export { firebaseApp };
