package scheduler;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Holds information due to timezones and language. Provides functionality for translations and conversion between
 * current and database time.
 * @author Ken Butler
 */
public class DefaultLocale {
    private final static HashMap<String, String> translation = new HashMap<>();
    private final static String locale = java.util.Locale.getDefault().toString().substring(0,2)+"_";
    private static final DateTimeFormatter sqlFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * A string representing the country
     */
    public final static String country = java.util.Locale.getDefault().getCountry();

    /**
     * <p>The ZoneID taken from the system upon application start.</p>
     * <p>Stored to prevent potential problems from user changing it on local end while program is running.</p>
     */
    public static ZoneId zone = ZoneId.systemDefault();

    /**
     * Initializes the translation map with pre-translated keys.
     */
    public static void init() {
        put("title","Appointment Scheduler","Planificateur De Rendez-vous");
        put("username","Username:","Nom d'utilisateur:");
        put("password","Password:","Mot de passe:");
        put("login","Login","Connexion");
        put("file","File","Dossier");
        put("preferences","Preferences...","Préférences...");
        put("help","Help","Aider");
        put("quit","Quit","Arrêter");
        put("about","About Appointment Scheduler","Sur Planificateur De Rendez-vous");
        put("need_username","Username cannot be empty.","Le nom d'utilisateur ne peut pas être vide.");
        put("need_password","Password cannot be empty.","Le mot de passe ne peut pas être vide.");
        put("incorrect_login","Incorrect username or password.","Identifiant ou mot de passe incorrect.");
        put("location", "Location","Emplacement");
        put("new_appointment","New Appointment","Nouveau Rendez-vous");
        put("duplicate_appointment","Duplicate Appointment","Dupliquer le Rendez-vous");
        put("modify_appointment","Edit Appointment","Modifier le Rendez-vous");
        put("delete_appointment","Delete Appointment","Supprimer le Rendez-vous");
        put("logout", "Logout","Déconnexion");
        put("calendar","Calendar","Calendrier");
        put("upcoming","Upcoming Appointments","Rendez-vous à Venir");
        put("navigation", "Navigation", "Navigation");
        put("admin","Admin","Admin");
        put("new_user","New User","Nouvel Utilisateur");
        put("user_list","User List","Liste D'utilisateur");
        put("january","January","Janvier");
        put("february","February","Février");
        put("march","March","Mars");
        put("april","April","Avril");
        put("may","May","Mai");
        put("june","June","Juin");
        put("july","July","Juillet");
        put("august","August","Août");
        put("september","September","Septembre");
        put("october","October","Octobre");
        put("november","November","Novembre");
        put("december","December","Décembre");
        put("num_appointments","Appointments: ","Rendez-vous: ");
        put("next_appointment","\nNext: ","\nPorchain: ");
        put("ongoing_appointment","\nOngoing: ","\nEn cours: ");
        put("no_appointment","\nNo upcoming appointments","\nAucun rendez-vous à venir");
        put("num_clients","Clients: ","Clients: ");
        put("week", "Week","Semaine");
        put("sunday", "Sunday", "Dimanche");
        put("monday", "Monday", "Lundi");
        put("tuesday", "Tuesday", "Mardi");
        put("wednesday", "Wednesday", "Mercredi");
        put("thursday", "Thursday", "Jeudi");
        put("friday", "Friday", "Vendredi");
        put("saturday", "Saturday", "Samedi");
        put("new_edit_appointment", "New/Edit Appointment", "Nouveau/Modifier Rendez-vous");
        put("edit","Edit","Modifier");
        put("new","New","Nouveau");
        put("appointment_title","Title","Titre");
        put("description", "Description","Description");
        put("start_time","Start Time","Heure de Début");
        put("end_time", "End Time", "Heure de Fin");
        put("customer","Customer","Client");
        put("user","User","Utilisateur");
        put("contact","Contact","Contacter");
        put("ok","OK","Accord");
        put("cancel","Cancel","Annuler");
        put("delete", "Delete", "Effacer");
        put("back", "<- Back", "<- Arriére");
        put("type", "Type","Taper");
        put("add_new", "Add New...","Ajouter nouveau...");
        put("ok_caps","OK","ACCORD");
        put("cancel_caps","CANCEL","ANNULER");
        put("delete_confirmation_appointments","Are you sure you want to delete customer '%1%'?\n\nThis will also delete %2% appointments:\n","Voulez-vous vraiment supprimer le client %1%?\n\nCela supprimera également %2% rendez-vous:\n");
        put("delete_confirmation","Are you sure you want to delete customer '%1%'?","Voulez-vous vraiment supprimer le client %1%?");
        put("warning","WARNING","AVERTISSEMENT");
        put("confirmation", "CONFIRM", "CONFIRMER");
        put("save", "Save", "Sauver");
        put("name", "Name","Nom");
        put("edit_customer_title","Edit Customers","Modifier les Clients");
        put("postal_code","Postal Code", "Code Postale");
        put("address","Address","Adresse");
        put("phone","Phone Number","Téléphone");
        put("country","Country","Pays");
        put("state","State","État");
        put("province", "Province", "Province");
        put("sovereignty","Sovereignty","Souveraineté");
        put("id", "ID","ID");
        put("division", "Division","Division");
        put("name_empty","Name must be longer than 2 characters.","Le nom doit comporter plus de 2 caractères.");
        put("phone_incorrect","Entered Phone Number incomplete or invalid.","Le numéro de téléphone saisi est incomplet ou non valide.");
        put("country_empty", "You must select a country.", "Vous devez choisir un pays");
        put("division_empty", "You must select a state, province, or sovereign.","Vous devez sélectionner un État, une Province ou un Souverain.");
        put("address_empty","Address cannot be less than 4 characters.","L'adresse ne peut pas comporter moins de 4 caractères.");
        put("location_empty","Postal code must be longer than 2 characters.","Le code postal doit comporter plus de 2 caractères.");
        put("month", "Month", "Mois");
        put("list", "List","Lister");
        put("appointment_details", "Appointment Details", "Détails du Rendez-vous");
        put("title_empty","Title must be longer than 2 characters.", "Titre doit comporter plus de 2 caractères.");
        put("type_empty", "Type must be longer than 2 characters.", "Taper doit comporter plus de 2 caractères.");
        put("time_empty","Starting and Ending times must be selected.", "Les heures de début et de fin doivent être sélectionnées.");
        put("customer_empty", "Please select the customer, or create a new one by pressing '+'.", "Veuillez sélectionner le client ou en créer un nouveau en appuyant sur '+'.");
        put("contact_empty", "Please select a contact.", "Veuillez sélectionner un contact.");
        put("appointment_location_empty", "Location must be longer than 2 characters.","L'emplacement doit comporter plus de 2 caractères.");
        put("date_empty","Please select a date for the appointment.","Veuillez sélectionner une date pour le rendez-vous.");
        put("days", " Days, ", " Jours, ");
        put("cancel_confirmation", "Are you sure you want to delete this appointment?\n\nAppointment ID: %1%\nAppointment Type: %2%\nCustomer: %3%","Voulez-vous vraiment supprimer ce rendez-vous?\n\nID de rendez-vous : %1%\nType de rendez-vous : %2%\nClient : %3%");
        put("yes_caps", "YES", "OUI");
        put("no_caps", "NO", "NON");
        put("date", "Date", "Date");
        put("upcoming_alarm", "You have an appointment starting within 15 minutes.\n\nAppointment details\nID: %1%\nStart Time: %2%","Vous avez un rendez-vous qui commence dans 15 minutes.\n\nDétails du rendez-vous\nID: %1%\nHeure de début: %2%");
        put("time_collision", "Customer has an existing appointment and is unavailable between %1% and %2%.", "Le client a un rendez-vous existant et n'est pas disponible entre %1% et %2%.");
        put("new_edit_customer", "New/Edit Customers", "Nouveaux/Modifier les Clients");
        put("reports", "Reports", "Rapports");
        put("customers_by_month", "Customers By Month/Type", "Clients par Mois/Type");
        put("customers_by_country", "Customers By Country", "Clients Par Pays");
        put("contact_schedule", "Contact Schedule", "Horaire des Contacts");
        put("login_failed_username", "Attempt to login failed due to missing username.\r\n", "La tentative de connexion a échoué en raison d'un nom d'utilisateur manquant.\r\n");
        put("login_failed_password", " - Attempt to login failed due to missing password.\r\n", " - La tentative de connexion a échoué en raison d'un mot de passe manquant.\r\n");
        put("login_failed_incorrect", " - Attempt to login failed due to incorrect username/password combination.\r\n", " - La tentative de connexion a échoué en raison d'une combinaison nom d'utilisateur/mot de passe incorrecte.\r\n");
        put("login_succeeded", " - Logged in.\r\n", " - Connecté.\r\n");
        put("logged_out", " - Logged out.\r\n"," - Déconnecté.\r\n");
        put("min_length", "Min Length of Appointments:", "Durée min des rendez-vous:");
        put("create_test_appointment", "Create a Test Appointment starting in:", "Créez un rendez-vous d'essai à partir de:");
        put("enable_testing_mode", "Enable Testing Mode?:", "Activer le mode test?:");
        put("close", "CLOSE", "PROCHE");
        put("year", "Year", "An");
        put("all", "ALL", "TOUS");
        put("appointment_count", "# Appointments", "# Rendez-vous");
        put("customer_count", "# Customers", "# Les clients");
    }

    /**
     * Adds a specific set of translations to the map. Can easily be expanded to add new languages.
     * @param key The key for the translated line.
     * @param english The line in English.
     * @param french The line in French.
     */
    public static void put(String key, String english, String french) {
        translation.put("en_"+key,english);
        translation.put("fr_"+key,french);
    }

    /**
     * Provides the translated string for the provided key based on detected locale on time of application start.
     * @param key The key to the translation.
     * @return The translated string.
     */
    public static String translate(String key) {
        String translated_key = locale+key;
        if(translation.containsKey(translated_key))
            return translation.get(translated_key);
        return key;
    }

    /**
     * Helper method to convert local zoned time into a string for use of updating a time in UTC on the database.
     * @param time The time with zone.
     * @return String converted to UTC as expected by SQL format.
     */
    public static String sqlDateTime(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneOffset.UTC).format(DefaultLocale.sqlFormat);
    }

    /**
     * Helper method to convert timestamps provided by JDBC to local zoned times. Zone is stored at application boot.
     * @param time The timestamp to convert.
     * @return The ZonedDateTime of stored zone. Will return null if provided timestamp was null.
     */
    public static ZonedDateTime userDateTime(Timestamp time) {
        if(time==null)return null;
        return time.toInstant().atZone(zone);
    }
}