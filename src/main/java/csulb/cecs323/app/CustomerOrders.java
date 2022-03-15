/*
 * Licensed under the Academic Free License (AFL 3.0).
 *     http://opensource.org/licenses/AFL-3.0
 *
 *  This code is distributed to CSULB students in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, other than educational.
 *
 *  2018 Alvaro Monge <alvaro.monge@csulb.edu>
 *
 */

package csulb.cecs323.app;

// Import all of the entity classes that we have written for this application.
import csulb.cecs323.model.*;
import org.apache.derby.impl.store.raw.log.Scan;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * A simple application to demonstrate how to persist an object in JPA.
 * <p>
 * This is for demonstration and educational purposes only.
 * </p>
 * <p>
 *     Originally provided by Dr. Alvaro Monge of CSULB, and subsequently modified by Dave Brown.
 * </p>
 * Licensed under the Academic Free License (AFL 3.0).
 *     http://opensource.org/licenses/AFL-3.0
 *
 *  This code is distributed to CSULB students in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, other than educational.
 *
 *  2021 David Brown <david.brown@csulb.edu>
 *
 */
public class CustomerOrders {
   /**
    * You will likely need the entityManager in a great many functions throughout your application.
    * Rather than make this a global variable, we will make it an instance variable within the CustomerOrders
    * class, and create an instance of CustomerOrders in the main.
    */
   private EntityManager entityManager;

   /**
    * The Logger can easily be configured to log to a file, rather than, or in addition to, the console.
    * We use it because it is easy to control how much or how little logging gets done without having to
    * go through the application and comment out/uncomment code and run the risk of introducing a bug.
    * Here also, we want to make sure that the one Logger instance is readily available throughout the
    * application, without resorting to creating a global variable.
    */
   private static final Logger LOGGER = Logger.getLogger(CustomerOrders.class.getName());

   /**
    * The constructor for the CustomerOrders class.  All that it does is stash the provided EntityManager
    * for use later in the application.
    * @param manager    The EntityManager that we will use.
    */
   public CustomerOrders(EntityManager manager) {
      this.entityManager = manager;
   }

   public static void main(String[] args) {
      LOGGER.fine("Creating EntityManagerFactory and EntityManager");
      EntityManagerFactory factory = Persistence.createEntityManagerFactory("CustomerOrders");
      EntityManager manager = factory.createEntityManager();
      // Create an instance of CustomerOrders and store our new EntityManager as an instance variable.
      CustomerOrders customerOrders = new CustomerOrders(manager);


      // Any changes to the database need to be done within a transaction.
      // See: https://en.wikibooks.org/wiki/Java_Persistence/Transactions

      LOGGER.fine("Begin of Transaction");
      EntityTransaction tx = manager.getTransaction();

      tx.begin();
      // List of Products that I want to persist.  I could just as easily done this with the seed-data.sql
      List <Products> products = new ArrayList<Products>();
      // Load up my List with the Entities that I want to persist.  Note, this does not put them
      // into the database.
      products.add(new Products("076174517163", "16 oz. hickory hammer", "Stanely Tools", "1", 9.97, 50));
      products.add(new Products("141174897222", "Bathroom storage unit", "Stanely Tools", "1", 49.99, 20));
      products.add(new Products("089174523163", "Wood flooring", "Stanely Tools", "1", 69.55, 37));
      // Create the list of owners in the database.
      customerOrders.createEntity (products);

      List <Customers> customers = new ArrayList<Customers>();
      customers.add(new Customers("Schmitt","Carine ","323 11th st. Carolina","900473", "232-232-8989"));
      customers.add(new Customers("King","Jean","453 18th st. Carolina","900473", "323-232-0000"));
      customers.add(new Customers("Ferguson","Peter","333 13th st. Carolina","900473", "982-232-9089"));
      // Create the list of owners in the database.
      customerOrders.createEntity (customers);

      // Commit the changes so that the new data persists and is visible to other users.
      tx.commit();
      LOGGER.fine("End of Transaction");

      //Customers customers = new Customers();
      //Products products_u = new Products();

      JFrame frame = new JFrame();
      //give the user an option to select their customer_id
      List<Long> customers_list = manager.createQuery("Select distinct (c.customer_id) from Customers c where c.customer_id is not null").getResultList();
      Object [] cl = customers_list.toArray();
      long user_id;
      frame.setAlwaysOnTop(true);
      user_id = Long.valueOf(String.valueOf(JOptionPane.showInputDialog(frame,"Customer ID", "Customers", JOptionPane.PLAIN_MESSAGE, null,cl ,customers_list.get(0))));

      List<Integer> product_quantity = new ArrayList<>();
      //show all products name to the user
      List<Long> products_name = manager.createQuery("Select distinct (p.prod_name) from Products p where p.prod_name is not null").getResultList();
      Object [] pl = products_name.toArray();

      //add the products the user selected to the list of products_selected
      List<String> products_selected = new ArrayList<>();
      frame.setAlwaysOnTop(true);
      products_selected.add(String.valueOf(JOptionPane.showInputDialog(frame,"Product Name", "Products", JOptionPane.PLAIN_MESSAGE, null,pl,products_name.get(0))));

      frame.setAlwaysOnTop(true);
      product_quantity.add(Integer.valueOf(JOptionPane.showInputDialog(frame,"How Many")));
      //check if we have that much quantity
      while(JOptionPane.showConfirmDialog(null, "Do you want to add more products") == JOptionPane.YES_OPTION ){
         String name_prod = String.valueOf(JOptionPane.showInputDialog(frame,"Product Name", "Products", JOptionPane.PLAIN_MESSAGE, null,pl,products_name.get(0)));
         products_selected.add(name_prod);
         frame.setAlwaysOnTop(true);
         int quantities = Integer.valueOf(JOptionPane.showInputDialog(frame,"How Many"));

         //validate that we have enough units in stock to sell
         if(quantities <= customerOrders.getProduct(name_prod).getUnits_in_stock())
            product_quantity.add(quantities);
         else {
            if (JOptionPane.showConfirmDialog(null, "We only have " + customerOrders.getProduct(name_prod).getUnits_in_stock()) == JOptionPane.YES_OPTION) {
               product_quantity.add(customerOrders.getProduct(name_prod).getUnits_in_stock());
            }
            else {
               //abort
               product_quantity.clear();
               products_selected.clear();
               System.exit(0);
            }
         }
         //check if we have that much quantity
      }

      //the order date and time
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:MM");
      LocalDateTime order_date_time = LocalDateTime.now();
      frame.setAlwaysOnTop(true);
      Object[] o_date_time = {order_date_time};

      //accept date and time then validate
      order_date_time = LocalDateTime.parse(JOptionPane.showInputDialog(frame, "Order Date and Time",order_date_time));
      if(!(order_date_time.equals(LocalDateTime.now()) || order_date_time.isAfter(LocalDateTime.now())))
         order_date_time = LocalDateTime.parse(JOptionPane.showInputDialog(frame, "Enter Valid Order Date and Time",order_date_time));

      //total price for the products the user is ordering
      double total_price = 0;
      for(int i = 0; i < products_selected.size(); i++){
         total_price += customerOrders.getProduct(products_selected.get(i)).getUnit_list_price() * product_quantity.get(0);
      }

      frame.setAlwaysOnTop(true);
      Object[] total_price_display = {total_price};
      if(JOptionPane.showConfirmDialog(frame,"Total Price: $" + total_price, "Total Due",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
         //add the record to order table first
         List<Order_lines> ol = new ArrayList<Order_lines>();
         List<Orders> orders = new ArrayList<Orders>();
         EntityTransaction tx2 = manager.getTransaction();

         tx2.begin();
         for (int k = 0; k < products_selected.size(); k++) {
            if (customers.get(k).getCustomer_id() == user_id) {
               orders.add(new Orders(customers.get(k), order_date_time, "Raymond"));
            }
         }
         //populate the order table
         customerOrders.createEntity(orders);

         //add the records to order_line table
         for (int k = 0; k < products_selected.size(); k++) {
            if (customers.get(k).getCustomer_id() == user_id) {
               //(int quantity, double unit_sale_price , Orders orders, Customers customers, Products products)
               ol.add(new Order_lines(product_quantity.get(k), customerOrders.getProduct(products_selected.get(k)).getUnit_list_price(), orders.get(0), orders.get(0).getCustomer(), customerOrders.getProduct(products_selected.get(k))));
               customerOrders.getProduct(products_selected.get(k)).setUnits_in_stock((customerOrders.getProduct(products_selected.get(k)).getUnits_in_stock()) - product_quantity.get(k));
            }                                                                                                                                    //50           - 2 = 48
         }
         //populate the order_line table
         customerOrders.createEntity(ol);
         tx2.commit();
         LOGGER.fine("End of Transaction");
         System.exit(0);
      }
      else {
         //abort
         customers_list.clear();
         product_quantity.clear();
         products_selected.clear();
         System.exit(0);
      }
   } // End of the main method

   /**
    * Create and persist a list of objects to the database.
    * @param entities   The list of entities to persist.  These can be any object that has been
    *                   properly annotated in JPA and marked as "persistable."  I specifically
    *                   used a Java generic so that I did not have to write this over and over.
    */
   public <E> void createEntity(List <E> entities) {
      for (E next : entities) {
         LOGGER.info("Persisting: " + next);
         // Use the CustomerOrders entityManager instance variable to get our EntityManager.
         this.entityManager.persist(next);
      }

      // The auto generated ID (if present) is not passed in to the constructor since JPA will
      // generate a value.  So the previous for loop will not show a value for the ID.  But
      // now that the Entity has been persisted, JPA has generated the ID and filled that in.
      for (E next : entities) {
         LOGGER.info("Persisted object after flush (non-null id): " + next);
      }
   } // End of createEntity member method

   /**
    * Think of this as a simple map from a String to an instance of Products that has the
    * same name, as the string that you pass in.  To create a new Cars instance, you need to pass
    * in an instance of Products to satisfy the foreign key constraint, not just a string
    * representing the name of the style.
    * @param UPC        The name of the product that you are looking for.
    * @return           The Products instance corresponding to that UPC.
    */
   public Products getProduct (String UPC) {
      // Run the native query that we defined in the Products entity to find the right style.
      List<Products> products = this.entityManager.createNamedQuery("ReturnProduct",
              Products.class).setParameter(1, UPC).getResultList();
      if (products.size() == 0) {
         // Invalid style name passed in.
         return null;
      } else {
         // Return the style object that they asked for.
         return products.get(0);
      }
   }// End of the getStyle method
} // End of CustomerOrders class
