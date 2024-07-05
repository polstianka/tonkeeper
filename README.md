# Tonkeeper Contest

This project pertains to the Tonkeeper contest. In this project the Swap and Buy/Sell features are implemented.


### Swap:

I have developed and written the code to perform a native swap without using any webview.  In this project, I have implemented swap natively only using Kotlin code (without using webview) and have not used any Javascript injection into a webview. This has resulted into a smooth swap experience and great performance. Also the maintainability and scalability of the project is much higher.
From the creation of the swap object bundle to signing it and sending it to the blockchain happens inside Kotlin environment.

### Buy/Sell:

I have also implemented Buy and sell feature where user can enter TON amount and select payment method and currency and then operator and finally is redirected to the operators web payment gateway. Then after successful Buy/Sell user is returned to the application.

### Important:
Because the tonkeeper android project is Work-In-Progress in order to be able to send token or swap token you should make sure that your wallet has been initialized.

### Test:
The changes made on this project have been tested on a wide variety of devices and all swap operations happen successfully.