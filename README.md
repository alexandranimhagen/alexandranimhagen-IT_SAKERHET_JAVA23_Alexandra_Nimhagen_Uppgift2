# TimecapsuleApp

TimecapsuleApp är en säker applikation för att skapa och hantera krypterade tidskapslar. Denna app låter användare spara privata meddelanden som kan hämtas och dekrypteras när som helst, men endast av rätt användare.

## Funktioner
- **Användarregistrering:** Skapa ett konto med e-post och lösenord (lösenord hashas med bcrypt).
- **Inloggning:** Logga in med JWT-baserad autentisering.
- **Skapa tidskapsel:** Skapa och kryptera ett meddelande med AES innan det lagras.
- **Hämta tidskapsel:** Dekryptera och läs sparade meddelanden.

## Teknologi
- **Backend:** Java med Spring Boot eller ren Java
- **Databas:** MySQL för att lagra användare och krypterade meddelanden
- **Autentisering:** JSON Web Tokens (JWT)
- **Kryptering:** Symmetrisk kryptering med AES
- **Frontend/klient:** Android-applikation byggd i Kotlin

## Installation och körning
### Backend
1. Klona detta repo:
   ```bash
   git clone https://github.com/alexandranimhagen/alexandranimhagen-IT_SAKERHET_JAVA23_Alexandra_Nimhagen_Uppgift2.git
