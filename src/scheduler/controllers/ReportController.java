package scheduler.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import scheduler.DefaultLocale;
import scheduler.JDBC;
import scheduler.Main;
import scheduler.records.Contact;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>Controller for the Report Scene.</p>
 * <p>Robust enough to support multiple types of reports. Different reports are loaded via
 * {@link ReportController#showContactSchedule()}, {@link ReportController#showCountryCount()}, and
 * {@link ReportController#showTypeMonthReport()}. Additional reports can be created by adding additional functions.</p>
 *
 * @author Ken Butler
 */
@SuppressWarnings({"ConstantConditions","unchecked","rawtypes"})
public class ReportController {
    /**
     * Instanced ReportController to be referenced from outside of the Controller or from a Static scope.
     */
    public static ReportController INSTANCE;

    private static Stage reportStage;

    @FXML AnchorPane ap_report_pane;
    @FXML Label l_filter;
    @FXML TableView<Report> tv_report_table;
    @FXML Button b_close, b_filter_back, b_filter_next;

    private final HashMap<Integer,String> filterMap = new HashMap<>();
    private final SimpleIntegerProperty selectedFilter = new SimpleIntegerProperty(-2);

    private final ObservableList<Report> reports = FXCollections.observableArrayList();
    private final FilteredList<Report> reportList = new FilteredList<>(reports);

    /**
     * Holds report data. Robust enough to handle as many reports as necessary.
     */
    private static class Report {
        private final List<Object> reportList;
        private int predicateColumn = -1;

        public Report(Object... list){
            reportList=Arrays.asList(list);
        }

        /**
         * Predicate checks is done via int fields. Allows dynamic reports to be filtered by IDs.
         * @param i The number of the column to check against predicate.
         * @return Returns itself to allow for constructor chaining.
         */
        public Report setPredicate(int i){
            predicateColumn=i;
            return this;
        }

        /**
         * Returns an int from the report entry.
         * @param col Column number.
         * @return Returns requested int, or -1 if either not found or not an int.
         */
        public int getInt(int col){
            Object o = reportList.get(col);
            if(o instanceof Integer)
                return (int)o;
            return -1;
        }

        /**
         * Returns a string from the report entry.
         * @param col Column number.
         * @return Returns requested string, or null if either not found or not a string.
         */
        public String getString(int col){
            Object o = reportList.get(col);
            if(o!=null)
                return o.toString();
            return null;
        }

        /**
         * Returns a ZonedDateTime from the report entry.
         * @param col Column number.
         * @return Returns requested int, or null if either not found or not a ZonedDateTime.
         */
        public ZonedDateTime getDateTime(int col){
            Object o = reportList.get(col);
            if(o instanceof ZonedDateTime)
                return (ZonedDateTime) o;
            return null;
        }

        /**
         * Checks predicate column against another integer.
         * @param i Integer to check
         * @return Whether data in the column matches the Integer.
         */
        public boolean predicate(int i){
            if(predicateColumn<0)return true;

            return getInt(predicateColumn)==i;
        }
    }

    /**
     * Loads the preference window stage and assigns the primary window as the owner.
     * @param primaryStage The Primary Window
     */
    public static void load(Stage primaryStage){
        try {
            reportStage = new Stage();
            Scene preferencesScene = new Scene(FXMLLoader.load(Main.class.getResource("scenes/Report.fxml")), 256, 200);
            reportStage.initModality(Modality.APPLICATION_MODAL);
            reportStage.initOwner(primaryStage);
            reportStage.setScene(preferencesScene);
        }catch (IOException ignored){}
    }

    /**
     * Initializes and translates the window and binds element positions so window can be resized.
     */
    @FXML
    private void initialize() {
        INSTANCE = this;

        tv_report_table.setTranslateX(10);
        tv_report_table.setTranslateY(45);
        tv_report_table.prefWidthProperty().bind(ap_report_pane.widthProperty().subtract(20));
        tv_report_table.prefHeightProperty().bind(ap_report_pane.heightProperty().subtract(90));
        tv_report_table.setColumnResizePolicy(policy->true);

        b_close.setText(DefaultLocale.translate("close"));
        b_close.translateXProperty().bind(ap_report_pane.widthProperty().subtract(10).subtract(b_close.widthProperty()));
        b_close.translateYProperty().bind(ap_report_pane.heightProperty().subtract(10).subtract(b_close.heightProperty()));

        b_filter_back.setTranslateX(20);
        b_filter_back.setTranslateY(10);

        b_filter_next.translateXProperty().bind(ap_report_pane.widthProperty().subtract(20).subtract(b_filter_next.widthProperty()));
        b_filter_next.setTranslateY(10);

        l_filter.translateXProperty().bind(ap_report_pane.widthProperty().divide(2).subtract(l_filter.widthProperty().divide(2)));
        l_filter.setTranslateY(13);

        SortedList<Report> sortedList = new SortedList<>(reportList);
        sortedList.comparatorProperty().bind(tv_report_table.comparatorProperty());
        tv_report_table.setItems(sortedList);

        reportList.setPredicate(report -> true);

        b_filter_next.setOnAction(actionEvent -> {
            Object[] set = filterMap.keySet().toArray();
            int i = -1;
            boolean found = false;
            for(Object o:set){
                int s = (int)o;
                if(s==selectedFilter.get())
                    found=true;
                else if(found) {
                    i = s;
                    break;
                }
            }
            selectedFilter.set(i);
        });
        b_filter_back.setOnAction(actionEvent -> {
            Object[] set = filterMap.keySet().toArray();
            int i = -1;
            if(selectedFilter.get()==-1){
                //Get last
                for(Object s:set)
                    i=(int)s;
            }
            else {
                for(Object s:set){
                    if((int)s==selectedFilter.get())
                        break;
                    else
                        i=(int)s;
                }
            }
            selectedFilter.set(i);
        });
        selectedFilter.addListener(change ->{
            l_filter.setText(filterMap.get(selectedFilter.get()));
            if(selectedFilter.get()<0)
                reportList.setPredicate(report -> true);
            else
                reportList.setPredicate(report -> report.predicate(selectedFilter.get()));
        });

        b_close.setOnAction(actionEvent -> reportStage.close());
    }

    /**
     * Clears data from the report table and removes columns so it can be refilled.
     */
    private static void clear(){
        INSTANCE.tv_report_table.getColumns().clear();
        INSTANCE.reports.clear();
        INSTANCE.reportList.setPredicate(report -> true);

        INSTANCE.l_filter.setVisible(false);
        INSTANCE.b_filter_back.setVisible(false);
        INSTANCE.b_filter_next.setVisible(false);

        INSTANCE.filterMap.clear();
        INSTANCE.filterMap.put(-1, DefaultLocale.translate("all"));
        INSTANCE.selectedFilter.set(-1);
    }

    /**
     * Resizes the window based on the width of the columns in the report table.
     */
    private static void resize() {
        double width = 45;

        //Calculate max-width per column
        for(TableColumn col:INSTANCE.tv_report_table.getColumns()) {
            Text t = new Text(col.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < INSTANCE.tv_report_table.getItems().size(); i++) {
                if (col.getCellData(i) != null) {
                    t = new Text(col.getCellData(i).toString());
                    double calc = t.getLayoutBounds().getWidth();
                    if (calc > max) {
                        max = calc;
                    }
                }
            }
            width += (max + 20.0d);
        }
        reportStage.setWidth(width);
    }

    /**
     * <p>Adds columns and data to the report window.</p>
     * <p>Will show the number of appointments grouped by Appointment Type and Month.</p>
     */
    public static void showTypeMonthReport() {
        //SELECT COUNT(Appointment_ID), Type, MONTH(Start), YEAR(Start) from appointments group by Type, MONTH(Start)+'-'+YEAR(Start);
        clear();
        reportStage.setTitle(DefaultLocale.translate("customers_by_month"));
        TableColumn<Report, Integer> tc_appointment_count = new TableColumn<>(DefaultLocale.translate("id"));
        tc_appointment_count.setCellValueFactory(reportIntegerCellDataFeatures -> new SimpleIntegerProperty(reportIntegerCellDataFeatures.getValue().getInt(0)).asObject());
        TableColumn<Report, String> tc_appointment_type = new TableColumn<>(DefaultLocale.translate("type"));
        tc_appointment_type.setCellValueFactory(reportStringCellDataFeatures -> new SimpleStringProperty(reportStringCellDataFeatures.getValue().getString(1)));
        TableColumn<Report, Integer> tc_appointment_month = new TableColumn<>(DefaultLocale.translate("month"));
        tc_appointment_month.setCellValueFactory(reportIntegerCellDataFeatures -> new SimpleIntegerProperty(reportIntegerCellDataFeatures.getValue().getInt(2)).asObject());
        TableColumn<Report, Integer> tc_appointment_year = new TableColumn<>(DefaultLocale.translate("year"));
        tc_appointment_year.setCellValueFactory(reportIntegerCellDataFeatures -> new SimpleIntegerProperty(reportIntegerCellDataFeatures.getValue().getInt(3)).asObject());
        INSTANCE.tv_report_table.getColumns().addAll(tc_appointment_count,tc_appointment_type,tc_appointment_month,tc_appointment_year);
        //Add data to report
        JDBC.process("SELECT COUNT(Appointment_ID), Type, MONTH(Start), YEAR(Start) from appointments group by Type, MONTH(Start)+'-'+YEAR(Start);", (set,row,count)->
            INSTANCE.reports.add(new Report(set.getInt(1),set.getString(2),set.getInt(3),set.getInt(4)))
        );
        resize();
        reportStage.showAndWait();
    }

    /**
     * <p>Adds columns and data to the report window.</p>
     * <p>Will list appointments filtered for each contact. Defaults to all contacts.</p>
     */
    public static void showContactSchedule() {
        //SELECT Appointment_ID, Title, Type, Description, Start, End, Customer_ID, Contact_ID from appointments ORDER BY Contact_ID, Start;
        clear();
        reportStage.setTitle(DefaultLocale.translate("contact_schedule"));

        //Enable filter
        INSTANCE.l_filter.setVisible(true);
        INSTANCE.b_filter_back.setVisible(true);
        INSTANCE.b_filter_next.setVisible(true);
        for(Contact c: Contact.getContacts()){
            INSTANCE.filterMap.put(c.id(), c.name());
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm a");

        //Add columns
        TableColumn<Report, Integer> report_id = new TableColumn<>(DefaultLocale.translate("id"));
        report_id.setCellValueFactory(reportIntegerCellDataFeatures -> new SimpleIntegerProperty(reportIntegerCellDataFeatures.getValue().getInt(0)).asObject());
        TableColumn<Report, String> report_title = new TableColumn<>(DefaultLocale.translate("appointment_title"));
        report_title.setCellValueFactory(reportStringCellDataFeatures -> new SimpleStringProperty(reportStringCellDataFeatures.getValue().getString(1)));
        TableColumn<Report, String> report_type = new TableColumn<>(DefaultLocale.translate("type"));
        report_type.setCellValueFactory(reportStringCellDataFeatures -> new SimpleStringProperty(reportStringCellDataFeatures.getValue().getString(2)));
        TableColumn<Report, String> report_description = new TableColumn<>(DefaultLocale.translate("description"));
        report_description.setCellValueFactory(reportStringCellDataFeatures -> new SimpleStringProperty(reportStringCellDataFeatures.getValue().getString(3)));
        TableColumn<Report, String> report_start = new TableColumn<>(DefaultLocale.translate("start_time"));
        report_start.setCellValueFactory(reportStringCellDataFeatures -> new SimpleStringProperty(reportStringCellDataFeatures.getValue().getDateTime(4).format(dtf)));
        TableColumn<Report, String> report_end = new TableColumn<>(DefaultLocale.translate("end_time"));
        report_end.setCellValueFactory(reportStringCellDataFeatures -> new SimpleStringProperty(reportStringCellDataFeatures.getValue().getDateTime(5).format(dtf)));
        TableColumn<Report, Integer> report_customer_id = new TableColumn<>(DefaultLocale.translate("customer"));
        report_customer_id.setCellValueFactory(reportIntegerCellDataFeatures -> new SimpleIntegerProperty(reportIntegerCellDataFeatures.getValue().getInt(6)).asObject());

        //Add data
        INSTANCE.tv_report_table.getColumns().addAll(report_id,report_title,report_type,report_description,report_start,report_end,report_customer_id);
        JDBC.process("SELECT Appointment_ID, Title, Type, Description, Start, End, Customer_ID, Contact_ID from appointments ORDER BY Contact_ID, Start;",(set,row,count)->
            INSTANCE.reports.add(new Report(set.getInt(1),set.getString(2),set.getString(3),set.getString(4),DefaultLocale.userDateTime(set.getTimestamp(5)),DefaultLocale.userDateTime(set.getTimestamp(6)),set.getInt(7),set.getInt(8)).setPredicate(7))
        );

        resize();
        reportStage.showAndWait();
    }

    /**
     * <p>Adds columns and data to the report window.</p>
     * <p>Will show the number of appointments and number of customers grouped by Customer's Country. Will get countries from
     * first_level_divisions in case of UK. Report is useful due to allowing resource pulling on customer languages.</p>
     */
    public static void showCountryCount() {
        //SELECT COUNT(Appointment_ID) AS Appointment_Count, COUNT(DISTINCT appointments.Customer_ID) AS Customer_Count, (CASE WHEN countries.Country="UK" THEN first_level_divisions.Division ELSE countries.Country END) AS Country from appointments LEFT JOIN customers on appointments.Customer_ID=customers.Customer_ID LEFT JOIN first_level_divisions on customers.Division_ID=first_level_divisions.Division_ID LEFT JOIN countries on first_level_divisions.Country_ID=countries.Country_ID GROUP BY (CASE WHEN countries.Country="UK" THEN customers.Division_ID ELSE first_level_divisions.Country_ID END) ORDER BY Appointment_Count DESC;
        clear();
        reportStage.setTitle(DefaultLocale.translate("customers_by_country"));

        //Add columns
        TableColumn<Report,Integer> appointment_count = new TableColumn<>(DefaultLocale.translate("appointment_count"));
        appointment_count.setCellValueFactory(reportIntegerCellDataFeatures -> new SimpleIntegerProperty(reportIntegerCellDataFeatures.getValue().getInt(0)).asObject());
        TableColumn<Report,Integer> customer_count = new TableColumn<>(DefaultLocale.translate("customer_count"));
        customer_count.setCellValueFactory(reportIntegerCellDataFeatures -> new SimpleIntegerProperty(reportIntegerCellDataFeatures.getValue().getInt(1)).asObject());
        TableColumn<Report,String> country = new TableColumn<>(DefaultLocale.translate("country"));
        country.setCellValueFactory(reportStringCellDataFeatures -> new SimpleStringProperty(reportStringCellDataFeatures.getValue().getString(2)));
        INSTANCE.tv_report_table.getColumns().addAll(appointment_count,customer_count,country);

        //Add data
        JDBC.process("SELECT COUNT(Appointment_ID) AS Appointment_Count, COUNT(DISTINCT appointments.Customer_ID) AS Customer_Count, (CASE WHEN countries.Country='UK' THEN first_level_divisions.Division ELSE countries.Country END) AS Country from appointments LEFT JOIN customers on appointments.Customer_ID=customers.Customer_ID LEFT JOIN first_level_divisions on customers.Division_ID=first_level_divisions.Division_ID LEFT JOIN countries on first_level_divisions.Country_ID=countries.Country_ID GROUP BY (CASE WHEN countries.Country='UK' THEN customers.Division_ID ELSE first_level_divisions.Country_ID END) ORDER BY Appointment_Count DESC;",(set,row,count)->
                INSTANCE.reports.add(new Report(set.getInt(1),set.getInt(2),set.getString(3)))
        );

        resize();
        reportStage.showAndWait();
    }
}
