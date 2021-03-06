package com.example.pavneetjauhal.smartwaiter;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pavneetjauhal on 15-11-16.
 *
 * Couchbase Lite Class used to manage Orders, Usser Account and
 * restaurant menus CouchBase instances on the device.
 *
 */
public class CouchBaseLite {
    /* Define global variables to be used in the Couchbase Lite class */
    private static final String DB_NAME = "restaurant_menus";
    private static final String DB_ORDER = "local_orders";
    private static final String DB_USER = "user_data";
    private static final String TAG = "SmartWaiter";
    //private static final String HOST = "http://192.168.0.35";
    //private static final String HOST = "http://192.168.43.200";
    private static final String HOST = "http://162.243.20.236";
    private static final String PORT = "4984";
    /* Key definitions for document */
    private static final String NAME = "Res_Name";
    private static final String CATEGORY = "category";
    public static String restaurant_Address = null;
    private static String timestamp = null;
    private static CouchBaseLite instance;
    Manager manager = null;
    Database database = null;
    Database database2 = null;
    Database database3 = null;
    User user = null;

    /*
    * Constructor to create the database instances.
    * Creates a new instance of database if one does not exist.
    * However, if an instance does exist, then simply returns
    * instance
    *
    * Input - ActivityContext, User
    */
    public CouchBaseLite(Context context, User user) throws IOException, CouchbaseLiteException {
        this.manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        this.database = manager.getDatabase(DB_NAME);
        this.database2 = manager.getDatabase(DB_ORDER);
        this.database3 = manager.getDatabase(DB_USER);
        this.user = user;
        Log.d(TAG, "################ Create Couch Base Lite database ################");
    }

    /*
    * Returns the CouchBase Lite instance to the caller
    *
    * Input - ActivityContext, User
    */
    public static CouchBaseLite getInstance(Context context, User user) {
        if (instance == null) {
            try {
                instance = new CouchBaseLite(context, user);
                return instance;
            } catch (IOException | CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /* Returns Menu database to the caller */
    public Database getMenuDatabase() throws CouchbaseLiteException {
        if ((this.database == null) & (this.manager != null)) {
            this.database = manager.getDatabase(DB_NAME);
        }
        return database;
    }
    /* Returns Orders database to the caller */
    public Database getOrderDatabase() throws CouchbaseLiteException {
        if ((this.database2 == null) & (this.manager != null)) {
            this.database2 = manager.getDatabase(DB_ORDER);
        }
        return database2;
    }
    /* Return User database to the user */
    public Database getUserDatabase() throws CouchbaseLiteException {
        if ((this.database3 == null) & (this.manager != null)) {
            this.database3 = manager.getDatabase(DB_USER);
        }
        return database3;
    }

    /*
    * Method to add all user information to local user database
    * Input - User userData
    */
    public void storeUserData(User userData) throws CouchbaseLiteException, NullPointerException {
        Document userdocument = this.getUserDatabase().getDocument("userData");
        Map<String, Object> properties = new HashMap<String, Object>();
        if (userData.getFirstName() != null) ;
        properties.put("First Name", userData.getFirstName());
        if (userData.getLastName() != null) ;
        properties.put("Last Name", userData.getLastName());
        if (userData.getPhoneNumber() != null) ;
        properties.put("Phone Number", userData.getPhoneNumber());
        if (userData.getPostalCode() != null) ;
        properties.put("Postal Code", userData.getPostalCode());
        if (userData.getBillingAddress() != null) ;
        properties.put("Home Address", userData.getBillingAddress());
        if (userData.getSalt() != null) ;
        properties.put("Salt", userData.getSalt());
        if (userData.getPassword() != null) ;
        properties.put("Password", userData.getPassword());
        userdocument.putProperties(properties);
    }

    /*
     * Extracts the information from the database and loads
     * it into memory.
     */
    public void populateUserData() throws CouchbaseLiteException {
        Document userdocument = this.getUserDatabase().getDocument("userData");
        if (userdocument.getProperties() != null
                && this.getUserDatabase().getDocument("userData") != null) {
            user.setFirstName((String) userdocument.getProperty("First Name"));
            user.setLastName((String) userdocument.getProperty("Last Name"));
            user.setBillingAddress((String) userdocument.getProperty("Address"));
            user.setPostalCode((String) userdocument.getProperty("Postal Code"));
            user.setPhoneNumber((String) userdocument.getProperty("Phone Number"));
            user.setSalt((String) userdocument.getProperty("Salt"));
            user.setPassword((String) userdocument.getProperty("Password"));
        } else {
            return;
        }
    }

    /* This method is used to extract the restaurant menu associated with specific barcode */
    public Document getRestaurantByBarcode(String barCode) {
        Document restaurantMenu = this.database.getDocument(barCode);
        return restaurantMenu;
    }

    /* Used for logging restaurant menu information in debug mode */
    public void outputContent(Document restaurantMenu) {
        Log.d(TAG, "###### Restaurant Menu Content ######" + restaurantMenu.getProperties());
    }

    /*
    * The function performs a database query by name and returns the
    * resulting name if match found. Also used for logging restaurant name
    * information in debug mode
    *
    * Input - Document restaurantMenu
    * Output - String restaurantName
    *  */
    public String getRestaurantName(Document restaurantMenu) {
        String restaurantName = (String) restaurantMenu.getProperty(this.NAME);
        Log.d(TAG, "###### Restaurant Name = " + restaurantName);
        return restaurantName;
    }

    /*
    * The function performs a database query by category and returns the
    * resulting category list if match found. Also, used for logging restaurant name
    * information in debug mode
    *
    * Input - Document restaurantMenu
    * Output - ArrayList categories
    *  */
    public ArrayList getCategoriesItems(Document restaurantMenu) {
        ArrayList category = new ArrayList();
        category = (ArrayList) restaurantMenu.getProperty(this.CATEGORY);
        Log.d(TAG, "###### Restaurant Category = " + category);
        return category;
    }

    /*
    * The function performs a database query for menu items and returns the
    * resulting items list if match found. Also, used for logging restaurant name
    * information in debug mode
    *
    * Input - Document restaurantMenu, String category
    * Output - ArrayList items
    *  */
    public ArrayList getMenuItems(Document restaurantMenu, String category) {
        ArrayList items = new ArrayList();
        items = (ArrayList) restaurantMenu.getProperty(category);
        Log.d(TAG, "###### Restaurant Items = " + items);
        return items;
    }

    /*
    * The function performs a database query for Category Names and returns the
    * resulting items list if match found. Also, used for logging restaurant categories
    * information in debug mode
    *
    * Input - ArrayList categoryItems
    * Output - ArrayList ListOfCategories
    *  */
    public List getCategoryNames(ArrayList categoryItems) {
        List<MenuCategories> menuList = new ArrayList<MenuCategories>();
        String categoryName = null;
        String categoryUrl = null;
        ArrayList categoryNamesList = new ArrayList();
        LinkedHashMap categoryNames = new LinkedHashMap();
        for (int i = 0; i < categoryItems.size(); i++) {
            categoryNames = (LinkedHashMap) categoryItems.get(i);
            Set set = categoryNames.entrySet();
            // Get an iterator
            Iterator j = set.iterator();
            // Display elements
            while (j.hasNext()) {
                Map.Entry me = (Map.Entry) j.next();

                if (me.getKey() == "type") {
                    categoryName = (String) me.getValue();
                    categoryNamesList.add(me.getValue());
                }
                if (me.getKey() == "url") {
                    categoryUrl = (String) me.getValue();
                    //categoryNamesList.add(me.getValue());
                }
                // Log.d(TAG, (me.getKey() + ": "));
                // Log.d(TAG, (String) me.getValue());
            }
            menuList.add(new MenuCategories(categoryName, categoryUrl));
            //Log.d(TAG, (String) menuList.get(i).getCategory());
            //Log.d(TAG, (String) categoryNamesList.get(i));
        }
        for (int x = 0; x < menuList.size(); x++) {
            Log.d(TAG, (String) menuList.get(x).getCategory());
        }
        return menuList;
    }

    /*
    * The function performs a database query for extracting customizable information
    * regarding menu items.
    *
    * Input - ArrayList categoryItems
    * Output - ArrayList ListOfCategories
    */
    public List getItemNames(ArrayList categoryItems) {
        List<MenuItems> itemList = new ArrayList<MenuItems>();
        String itemName = null;
        String itemPrice = null;
        String itemDetail = null;
        ArrayList<String> itemToppings = new ArrayList();
        ArrayList<String> itemSides = new ArrayList();
        itemSides = null;
        itemToppings = null;
        //ArrayList categoryNamesList = new ArrayList();
        LinkedHashMap categoryItemsHash = new LinkedHashMap();
        for (int i = 0; i < categoryItems.size(); i++) {
            categoryItemsHash = (LinkedHashMap) categoryItems.get(i);
            Set set = categoryItemsHash.entrySet();
            // Get an iterator
            Iterator j = set.iterator();
            // Display elements
            while (j.hasNext()) {
                Map.Entry me = (Map.Entry) j.next();

                if (me.getKey() == "name") {
                    itemName = (String) me.getValue();
                    //categoryNamesList.add(me.getValue());
                }
                if (me.getKey() == "price") {
                    itemPrice = (String) me.getValue();
                    //categoryNamesList.add(me.getValue());
                }
                if (me.getKey() == "details") {
                    itemDetail = (String) me.getValue();
                    //categoryNamesList.add(me.getValue());
                }
                if (me.getKey() == "toppings") {
                    ArrayList<Object> temp = new ArrayList();
                    temp.add(me.getValue());
                    itemToppings = (ArrayList) temp.get(0);
                } else {
                    itemToppings = null;
                }
                if (me.getKey() == "sides") {
                    ArrayList<Object> temp2 = new ArrayList();
                    temp2.add(me.getValue());
                    itemSides = (ArrayList) temp2.get(0);
                }
            }
            itemList.add(new MenuItems(itemName, itemPrice, itemDetail, itemToppings, itemSides));
        }
        for (int x = 0; x < itemList.size(); x++) {
            Log.d(TAG, (String) itemList.get(x).getItemName());
        }
        return itemList;
    }

    /* Method used for testing for now. Can be used later to query all docs */
    public void queryAllRestautant() {
        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            QueryRow row = it.next();
            Log.d(TAG, "#### All #### " + row.getDocumentId());
        }
    }

    /*
    * Method to construct URL to reach the cloud database
    * Input - String host, String port, String database_name
    * Output - Returns the constructed URL in string
    *
    */
    private URL createSyncURL(String host, String port, String db_name)
            throws MalformedURLException {
        URL syncURL = null;
        syncURL = new URL(host + ":" + port + "/" + db_name);
        return syncURL;
    }

    /*
    * Method to populate the ordered items into the database instance
     *
    * Input - List userItems
    * Output - List orderItems
    *
    */
    public List<OrderItems> populateOderitems(List<UserItems> userItems) {
        List<OrderItems> orderItems = new ArrayList<>();
        for (int i = 0; i < userItems.size(); i++) {
            orderItems.add(
                    new OrderItems(userItems.get(i).getItemName(), userItems.get(i).getItemPrice(),
                            userItems.get(i).getItemToppings(), userItems.get(i).getSideOrder(),
                            userItems.get(i).getSpecialInstrucitons()));
        }
        return orderItems;
    }

    /*
     * Method to convert all user information into a record and send it off
     * to the cloud database. Used to send orders to the cloud.
     *
     * Input - List UserItems
     */
    public void createItem(List<UserItems> UserItems) throws Exception {
        /* Truncate barcode to extract the table number */
        String tableNumber = MainActivity.qrCode.substring(MainActivity.qrCode.indexOf('-') + 1,
                MainActivity.qrCode.length());
        timestamp = new String(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("Table", tableNumber);
        properties.put("First Name", user.getFirstName());
        properties.put("UserName", user.getUsername());
        properties.put("Last Name", user.getLastName());
        properties.put("Total price", user.getTotalPrice());
        List<OrderItems> orderItems = new ArrayList<OrderItems>();
        orderItems = populateOderitems(UserItems);
        properties.put("Items List", orderItems);
        properties.put("Token", LoginActivity.user.getToken());
        properties.put("Address", user.getBillingAddress());
        properties.put("Phone Number", user.getPhoneNumber());
        properties.put("Postal Code", user.getPostalCode());
        properties.put("Current Time", timestamp);
        Document document = this.getOrderDatabase().getDocument(timestamp);
        document.putProperties(properties);
        setpushfilter(timestamp);
    }

    /*
     * Method used to setup a continuous push filter, which will
     * attempt to send the orders on a continuous bases
     *
     * Input - String timestamp
     */
    public void setpushfilter(final String timestamp)
            throws CouchbaseLiteException, MalformedURLException {
        // Define a filter that matches only docs with a given "Current Time" property.
        // The value to match is given as a parameter named "Current Time":

        this.getOrderDatabase().setFilter("Current Time", new ReplicationFilter() {
            @Override
            public boolean filter(SavedRevision revision, Map<String, Object> params) {
                assert revision != null;
                return revision.getProperty("Current Time") != null && revision.getProperty(
                        "Current Time").equals(timestamp);
            }
        });
        //
        // Set up a filtered push replication using the above filter block,
        // that will push only docs whose "owner" property equals "Waldo":
        Replication push = this.getOrderDatabase()
                .createPushReplication(this.createSyncURL(HOST, PORT, restaurant_Address));
        push.setFilter("Current Time");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Current Time", timestamp);
        push.setFilterParams(params);
        push.start();
        push.setContinuous(true);
    }


    /*
     * Method used to setup a continuous pull filter, which will
     * attempt to download menus from the remote database.
     *
     * Input - None
     */
    public void startReplications() throws CouchbaseLiteException, MalformedURLException {
        final Replication pull =
                this.getMenuDatabase().createPullReplication(this.createSyncURL(HOST, PORT, DB_NAME));
        pull.setContinuous(true);
        pull.start();

        pull.addChangeListener(new Replication.ChangeListener()

                               {
                                   @Override
                                   public void changed(Replication.ChangeEvent event) {
                                       // will be called back when the pull replication status changes
                                       if (pull.getStatus() == Replication.ReplicationStatus.REPLICATION_IDLE) {
                                           Log.d(TAG,
                                                   "################ The replication is complete #####################");
                                       } else {
                                           Log.d(TAG, "################ The replication Failed #####################");
                                       }
                                   }
                               }

        );
    }
}