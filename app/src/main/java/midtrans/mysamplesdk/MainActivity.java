package midtrans.mysamplesdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.midtrans.sdk.corekit.callback.CardRegistrationCallback;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.LocalDataHandler;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.PaymentMethod;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.CardRegistrationResponse;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.UserAddress;
import com.midtrans.sdk.corekit.models.UserDetail;
import com.midtrans.sdk.corekit.models.snap.CreditCard;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TransactionFinishedCallback {
    private Button buttonUiKit, buttonDirectCreditCard, buttonDirectBcaVa, buttonDirectMandiriVa,
            buttonDirectBniVa, buttonDirectAtmBersamaVa, buttonDirectPermataVa;
    private TextView editText, editText2, snapTokenEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind view
        bindViews();
        // Init sdk
        initSdk();
        // Init button sdk
        initActionButtons();
    }


    @Override
    public void onTransactionFinished(TransactionResult result) {
        if (result.getResponse() != null) {
            switch (result.getStatus()) {
                case TransactionResult.STATUS_SUCCESS:
                    Toast.makeText(this, "Transaction Finished. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(this, "Transaction Pending. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    break;
                case TransactionResult.STATUS_FAILED:
                    Toast.makeText(this, "Transaction Failed. ID: " + result.getResponse().getTransactionId() + ". Message: " + result.getResponse().getStatusMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
            result.getResponse().getValidationMessages();
        } else if (result.isTransactionCanceled()) {
            Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_LONG).show();
        } else {
            if (result.getStatus().equalsIgnoreCase(TransactionResult.STATUS_INVALID)) {
                Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Transaction Finished with failure.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void bindViews() {
        buttonUiKit = findViewById(R.id.button_uikit);
        buttonDirectCreditCard = findViewById(R.id.button_direct_credit_card);
        buttonDirectBcaVa = findViewById(R.id.button_direct_bca_va);
        buttonDirectMandiriVa = findViewById(R.id.button_direct_mandiri_va);
        buttonDirectBniVa = findViewById(R.id.button_direct_bni_va);
        buttonDirectPermataVa = findViewById(R.id.button_direct_permata_va);
        buttonDirectAtmBersamaVa = findViewById(R.id.button_direct_atm_bersama_va);

        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);

        editText2.setText(BuildConfig.MERCHANT_BASE_URL);
        editText.setText(BuildConfig.MERCHANT_CLIENT_KEY);

        snapTokenEditText = findViewById(R.id.snap_token_edittext);
    }

    private void initSdk() {
        String client_key = BuildConfig.MERCHANT_CLIENT_KEY;
        String base_url = BuildConfig.MERCHANT_BASE_URL;

        SdkUIFlowBuilder.init()
                .setClientKey(client_key) // client_key is mandatory
                .setContext(MainActivity.this) // context is mandatory
                .setTransactionFinishedCallback(MainActivity.this) // set transaction finish callback (sdk callback)
                .setMerchantBaseUrl(base_url) //set merchant url
                .enableLog(true) // enable sdk log
                .setColorTheme(new CustomColorTheme("#FFE51255", "#B61548", "#FFE51255")) // will replace theme on snap theme on MAP
                .buildSDK();
    }

    private CustomerDetails initCustomerDetails() {
        //define customer detail (mandatory for coreflow)
        CustomerDetails mCustomerDetails = new CustomerDetails();
        mCustomerDetails.setPhone("085310102020");
        mCustomerDetails.setFirstName("user fullname");
        mCustomerDetails.setEmail("mail@mail.com");
        return mCustomerDetails;
    }

    private CreditCard initCreditCard(){
        CreditCard creditCard = new CreditCard();
        creditCard.setSaveCard(false);
        creditCard.setSecure(true);
        creditCard.setAuthentication(CreditCard.AUTHENTICATION_TYPE_3DS);
        return creditCard;
    }

    private TransactionRequest initTransaction() {
        // Create new Transaction Request
        TransactionRequest transactionRequestNew = new TransactionRequest("order_id_" + System.currentTimeMillis(), 20000);

        // set customer details
        transactionRequestNew.setCustomerDetails(initCustomerDetails());

        // set credit card
        transactionRequestNew.setCreditCard(initCreditCard());

        // set item details
        ItemDetails itemDetails = new ItemDetails("1", 20000, 1, "Trekking Shoes");

        // Add item details into item detail list.
        ArrayList<ItemDetails> itemDetailsArrayList = new ArrayList<>();
        itemDetailsArrayList.add(itemDetails);
        transactionRequestNew.setItemDetails(itemDetailsArrayList);

        UserDetail userDetail = LocalDataHandler.readObject("user_details", UserDetail.class);
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setUserFullName("Robin Y");
            userDetail.setEmail("robin.rootad@gmail.com");
            userDetail.setPhoneNumber("0812123456789");
            ArrayList<UserAddress> userAddresses = new ArrayList<>();
            UserAddress userAddress = new UserAddress();
            userAddress.setAddress("bab 5");
            userAddress.setCity("Surabaya");
            userAddress.setAddressType(com.midtrans.sdk.corekit.core.Constants.ADDRESS_TYPE_BOTH);
            userAddress.setZipcode("60113");
            userAddress.setCountry("IDN");
            userAddresses.add(userAddress);
            userDetail.setUserAddresses(userAddresses);
            LocalDataHandler.saveObject("user_details", userDetail);
        }

        return transactionRequestNew;
    }

    private void initActionButtons() {
        buttonUiKit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String snapToken =  snapTokenEditText.getText().toString();
                MidtransSDK midtransSDK = MidtransSDK.getInstance();
                midtransSDK.setTransactionRequest(initTransaction());
                if(snapToken.equals("")){
                    midtransSDK.startPaymentUiFlow(MainActivity.this);
                } else {
                    midtransSDK.startPaymentUiFlow(MainActivity.this,snapToken);
                }
            }
        });

        buttonDirectCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransaction());
                MidtransSDK.getInstance().UiCardRegistration(MainActivity.this, new CardRegistrationCallback() {
                    @Override
                    public void onSuccess(CardRegistrationResponse cardRegistrationResponse) {
                        Toast.makeText(MainActivity.this, "register card token success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(CardRegistrationResponse cardRegistrationResponse, String s) {
                        Toast.makeText(MainActivity.this, "register card token Failed", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            }
        });


        buttonDirectBcaVa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransaction());
                MidtransSDK.getInstance().startPaymentUiFlow(MainActivity.this, PaymentMethod.BANK_TRANSFER_BCA);
            }
        });


        buttonDirectBniVa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransaction());
                MidtransSDK.getInstance().startPaymentUiFlow(MainActivity.this, PaymentMethod.BANK_TRANSFER_BNI);
            }
        });

        buttonDirectMandiriVa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransaction());
                MidtransSDK.getInstance().startPaymentUiFlow(MainActivity.this, PaymentMethod.BANK_TRANSFER_MANDIRI);
            }
        });


        buttonDirectPermataVa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransaction());
                MidtransSDK.getInstance().startPaymentUiFlow(MainActivity.this, PaymentMethod.BANK_TRANSFER_PERMATA);
            }
        });

        buttonDirectAtmBersamaVa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransaction());
                MidtransSDK.getInstance().startPaymentUiFlow(MainActivity.this, PaymentMethod.BCA_KLIKPAY);
            }
        });

    }

}