# FAMPE WebApp

Enkel webbversion av FAMPE-appen för iPhone och andra enheter.

## Setup
1. Kopiera `config.example.js` till `config.js`.
2. Fyll i dina API-nycklar i `config.js` (denna fil ignoreras av Git).
3. Installera Firebase CLI: `npm install -g firebase-tools`
4. Logga in: `firebase login`
5. Deploya: `firebase deploy --only hosting`

**Viktigt**: Pusha aldrig `config.js` till GitHub – den innehåller känsliga nycklar!

## Funktioner
- Google-inloggning
- Karta med användarens plats
- Poänglista (leaderboard) från Firestore
- Användarsida med UID

## Användning
- Öppna i Safari på iPhone.
- För PWA: Tryck på dela > "Lägg till på hemskärmen".

Flikar: Karta, Poänglista, Användare.