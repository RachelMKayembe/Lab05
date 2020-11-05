package mgarzon.createbest.productcatalog;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseProducts;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        listViewProducts = (ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct = (Button) findViewById(R.id.addButton);

        products = new ArrayList<>();


        //adding an onclicklistener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });
        databaseProducts = database.getReference("products");
    }


    @Override
    protected void onStart() {
        super.onStart();
        databaseProducts.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                products.clear();

                //iterating through all the nodes
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){ // for child in children (key; keys)
                    //getting product(getting value for on child)
                    Product product = postSnapshot.getValue(Product.class);
                    // adding product to the list
                    products.add(product);

                }
                //on doit adapter notre liste de produit pour qu'elle puisse bien entrer dans notre listeview

                //creating adapter
                ProductList productsAdapter = new ProductList(MainActivity.this, products); // adapter les contenues de notre product
                //attaching adapter to the Listview
                listViewProducts.setAdapter(productsAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {//ajoute un toast pour voir e message de l erreur (convertie erreur en message)
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();

            }
        });
    }



    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    // click on element to make an update
    private void updateProduct(String id, String name, double price) {

        // getting the specified product reference (by referring to the id)
        DatabaseReference productUpd = FirebaseDatabase.getInstance().getReference("products").child(id);

        // updating product with the same id
        Product product = new Product(id,name,price);
        productUpd.setValue(product);

        //success message
        Toast.makeText(this,"Product updated",Toast.LENGTH_LONG).show();

    }

    private boolean deleteProduct(String id) {

        // getting the specified product reference(by referring to id)
        DatabaseReference productDel = FirebaseDatabase.getInstance().getReference("products").child(id);

        // removing product
        productDel.removeValue();

        // removing message
        Toast.makeText(this,"Product Deleted",Toast.LENGTH_LONG).show();
        return true;


    }

    private void addProduct() {
        // getting value from the editText to save
        String name = editTextName.getText().toString().trim();
        double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));

        if(!TextUtils.isEmpty(name)){
            //it will create a unique key id and will be use as the primary Key for our Product
            String id = databaseProducts.push().getKey();

            // creating a product object
            Product product = new Product(id, name, price);

            //adding product to our database
            databaseProducts.child(id).setValue(product);

            // setting editText to a blanc again
            editTextName.setText("");
            editTextPrice.setText("");


            //display the success message
            Toast.makeText(this,"Product added",Toast.LENGTH_LONG).show();

        }else{
            //if the value is not giving, display a toast
            Toast.makeText(this,"Please enter a name",Toast.LENGTH_LONG).show();

        }

    }
}