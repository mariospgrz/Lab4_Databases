CEI325 – Lab 4: Tournament Platform

1. Πληροφορίες Ομάδας

- Μάθημα: CEI325 – Βάσεις Δεδομένων
- Εργαστήριο: Lab 4 – Tournament Platform
- Μέλη:
  - Βαφειάδης Παρασκευάς – Α.Φ.Τ: 27407
  - Ανδρέου Μάριος – Α.Φ.Τ: 27753
  - Γιαννή Αντρέας – Α.Φ.Τ: 27944
  - Αναστασίου Άδωνης – Α.Φ.Τ: 30804
2. Περιγραφή Εφαρμογής

Η εργασία υλοποιεί μια εφαρμογή διαχείρισης τουρνουά τύπου "Tournament Platform" χρησιμοποιώντας:

- MySQL βάση δεδομένων
- JDBC για σύνδεση Java–MySQL
- SQL scripts για δημιουργία πινάκων & constraints
- Εφαρμογή γραμμής εντολών (CLI)

Η εφαρμογή παρέχει μενού για:

- Manage Players
- Manage Tournaments
- Manage Participants
- Manage Matches

Όλες οι λειτουργίες εκτελούνται μέσω του κεντρικού μενού της `DBConnection.java`.

3. Δομή Project:

├── erd/
│ ├── Erd.png
│ └── Erd code.mmd
| └── README.md
│
├── lib/
│ └── mysql-connector-j.jar
│
├── out/
│
├── sql/
│ ├── 01-Lab4 Create tables.sql
│ └── 02-LAB4 ADD CONSTRAINTS.sql
│
├── src/main/java/
│ ├── DBConnection.java # Κύρια κλάση 
│ ├── PlayerManagement.java # Player management menu + logic
│ ├── TournamentManagement.java # Tournament management menu
│ ├── ParticipantManagement.java # Participant management menu
│ └── MatchManagement.java # Match management menu

4. Ρυθμίσεις Σύνδεσης (DBConnection.java)

Η σύνδεση στη βάση γίνεται με τις ακόλουθες σταθερές:

public static final String URL =
"jdbc:mysql://localhost:3306/lab4_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

public static final String USERNAME = "root";
public static final String PASSWORD = "PASSWORD"; #Εδώ βάζετε των κωδικό που έχετε στο root.

Οδηγίες για εκτέλεση προγράμματος:

Για να τρέξετε το πρόγραμμα πρέπει πρώτα να δημιουργήσετε τη βάση δεδομένων lab_4db χρησιμοποιώντας το query που βρίσκεται στο φάκελο SQL με όνομα 01-Lab4_Create table query.sql . Επιπλέον πρέπει να εισάγετε τα Constraints χρησιμοποιώντας το 02-LAB4_ADD CONSTRAINTS.sql query.

Έπειτα σε ένα terminal τρέξτε την παρακάτω εντολή για να κάνετε compile το κώδικα:
javac -cp ".;lib\mysql-connector-j.jar" -d out src\main\java\*.java

Μετά τρέξτε την παρακάτω εντολή για να τρέξετε το πρόγραμμα:
java -cp ".;out;lib\mysql-connector-j.jar" DBConnection.

Τέλος μπορείτε να χρησιμοποιήσετε το μενού που παρέχεται στην αρχή για να Δημιουργήσετε ή να διαχειριστείτε παίκτες, να δημιουργήσετε ή να διαχειριστείτε τουρνουά, συμμετέχοντες σε τουρνουά και ματς. Δημιουργώντας 2 ή περισσότερους παίκτες μπορείτε να χρησιμοποιήσετε τους παίκτες αυτούς για να τους βάλετε σε τουρνουά ως participants και μετά να τους βάλετε να αγωνιστούν σε matches δίνοντας το σκορ του κάθε παίκτη για να βρεθεί ο νικητής και ο ηττημένος του κάθε match. Επιπλέον μπορείτε να δείτε πληροφορίες για τον κάθε παίκτη ξεχωριστά είτε από το ID ή το Username του και να δείτε σχετικές πληροφορίες για το κάθε τουρνουά ,  συμμετεχόντων σε ένα τουρνουά και για κάθε match.
