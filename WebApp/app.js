// Importera config (skapa config.js från config.example.js)
import { firebaseConfig, mapsApiKey } from './config.js';

// Initiera Firebase
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore();

// Google Auth Provider
const provider = new firebase.auth.GoogleAuthProvider();

// DOM-element
const loginBtn = document.getElementById('loginBtn');
const logoutBtn = document.getElementById('logoutBtn');
const userInfo = document.getElementById('userInfo');
const mapTab = document.getElementById('mapTab');
const leaderboardTab = document.getElementById('leaderboardTab');
const userTab = document.getElementById('userTab');
const mapTabContent = document.getElementById('mapTabContent');
const leaderboardTabContent = document.getElementById('leaderboardTabContent');
const userTabContent = document.getElementById('userTabContent');
const leaderboardList = document.getElementById('leaderboardList');
const userDetails = document.getElementById('userDetails');

// Flik-funktionalitet
function showTab(tabContent) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    tabContent.classList.add('active');
}

mapTab.addEventListener('click', () => showTab(mapTabContent));
leaderboardTab.addEventListener('click', () => showTab(leaderboardTabContent));
userTab.addEventListener('click', () => showTab(userTabContent));

// Logga in
loginBtn.addEventListener('click', () => {
    auth.signInWithPopup(provider).then((result) => {
        console.log('Inloggad:', result.user);
    }).catch((error) => {
        console.error('Fel vid inloggning:', error);
    });
});

// Logga ut
logoutBtn.addEventListener('click', () => {
    auth.signOut().then(() => {
        console.log('Utloggad');
    });
});

// Lyssnare på auth-tillstånd
auth.onAuthStateChanged((user) => {
    if (user) {
        userInfo.textContent = `Inloggad som: ${user.displayName}`;
        loginBtn.style.display = 'none';
        logoutBtn.style.display = 'block';
        loadUserDetails(user);
    } else {
        userInfo.textContent = 'Inte inloggad';
        loginBtn.style.display = 'block';
        logoutBtn.style.display = 'none';
        userDetails.textContent = '';
    }
});

// Ladda leaderboard
function loadLeaderboard() {
    db.collection('players').orderBy('score', 'desc').onSnapshot((snapshot) => {
        leaderboardList.innerHTML = '';
        snapshot.forEach((doc, index) => {
            const player = doc.data();
            const li = document.createElement('li');
            li.textContent = `${index + 1}. ${player.name || doc.id} - ${player.score} poäng`;
            leaderboardList.appendChild(li);
        });
    });
}

// Ladda användardetaljer
function loadUserDetails(user) {
    userDetails.textContent = `UID: ${user.uid}`;
    // Här kan du lägga till mer info från Firestore om användaren
}

// Google Maps
function loadMapsAPI() {
    const script = document.createElement('script');
    script.src = `https://maps.googleapis.com/maps/api/js?key=${mapsApiKey}`;
    script.onload = initMap;
    document.head.appendChild(script);
}

function initMap() {
    const map = new google.maps.Map(document.getElementById('map'), {
        center: { lat: 59.3293, lng: 18.0686 }, // Stockholm som exempel
        zoom: 10
    });

    // Hämta användarens plats om tillåtet
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((position) => {
            const pos = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };
            map.setCenter(pos);
            new google.maps.Marker({
                position: pos,
                map: map,
                title: 'Din plats'
            });
        });
    }
}

// Initiera när sidan laddas
window.onload = () => {
    loadMapsAPI();
    loadLeaderboard();
};