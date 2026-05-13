# 🩸 BloodLink — Desktop Application

BloodLink est une application de bureau complète développée en **JavaFX** pour la gestion intégrée du don de sang. Elle couvre l'ensemble du cycle — de l'inscription des donneurs à la collecte, en passant par la gestion des stocks, les campagnes de sensibilisation et le suivi médical — le tout via une interface moderne et fluide.

L'application est conçue pour fonctionner en tandem avec une application web Symfony, partageant la même base de données MySQL et un système d'authentification entièrement compatible.

---

##  Fonctionnalités

###  Authentification & Sécurité
- **Google OAuth 2.0** — connexion et inscription via compte Google
- **Face ID** — authentification biométrique par reconnaissance faciale
- Système *Remember Me* pour la persistance de session
- Gestion des rôles

###  Gestion des Utilisateurs (Admin)
- Tableau de bord administrateur avec statistiques globales
- CRUD complet des utilisateurs
- Formulaires spécialisés pour les profils **Client** et **Agent Banque**

###  Dossiers Médicaux & Dons
- Création et suivi des dossiers médicaux des donneurs
- Historique complet des dons pour chaque utilisateur
- Gestion des dons

###  Rendez-vous
- Planification de rendez-vous de don de sang
- Questionnaire de pré-don intégré
- **Synchronisation automatique avec Google Calendar** via OAuth 2.0
- Vue calendrier interactive avec CalendarFX
- Gestion administrative des rendez-vous

###  Campagnes de Collecte
- Création et gestion de campagnes de don de sang
- Gestion des entités de collecte (centres, hôpitaux)
- **Carte interactive** des entités de collecte
- Vue calendrier des campagnes
- Interface dédiée pour les agents CNTS

###  Gestion Banque de Sang
- Espace agent banque avec gestion des demandes de sang
- Gestion des transferts inter-établissements
- Validation des demandes avec workflow de traitement
- **Compatibilité sanguine** — vérification automatique de la compatibilité des groupes sanguins

###  Stocks & Commandes
- Suivi des stocks de poches de sang par type
- Système de commandes avec gestion complète
- **Paiement en ligne via Stripe Checkout**

###  Intelligence Artificielle
- **Chatbot IA** intégré alimenté par l'API **Gemini**
- Génération automatique de rapports avec analyse IA
- Génération intelligente de descriptions de campagnes
- **Prédictions KNN** — modèle de Machine Learning pour les prédictions de besoins en don

###  Rapports & Export
- Génération de **rapports PDF**
- Export des données en **fichiers Excel**
- Rapports de prédiction avec graphiques et analyses

## 🛠️ Stack Technique

| Catégorie | Technologies |
|---|---|
| **Langage** | Java 17 |
| **Framework UI** | JavaFX 21 (Controls, FXML, Web) |
| **Build** | Maven |
| **Base de données** | MySQL (Connector/J 9.6) |
| **Authentification** | jBCrypt, Google OAuth 2.0 Client |
| **Calendrier** | Google Calendar API, CalendarFX |
| **Paiement** | Stripe Checkout API |
| **IA / ML** | Gemini API (via OkHttp3), KNN custom |
| **PDF** | iTextPDF, OpenPDF, Apache PDFBox |
| **Excel** | Apache POI |
| **E-mail** | JavaMail API |
| **Webcam** | Sarxos Webcam Capture |
| **Icônes** | Ikonli (FontAwesome 5) |
| **HTTP** | OkHttp3, Google HTTP Client (Gson) |
| **Config** | Dotenv-java |

---
## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à ouvrir une **issue** pour signaler un bug ou une **pull request** pour proposer une amélioration.
