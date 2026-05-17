Paiement TLS ChriOnline
=======================

Ce serveur TLS est separe du serveur TCP principal. Il sert a demontrer le tutoriel
"Paiement securise avec TLS (SSLSocket)" sans casser les autres fonctionnalites.

1. Generer le keystore serveur :

```powershell
keytool -genkeypair -alias payment-server -keyalg RSA -keysize 2048 -validity 365 `
  -keystore chrionline-server/src/main/resources/tls/payment-keystore.jks `
  -storepass changeit -keypass changeit `
  -dname "CN=localhost, OU=ChriOnline, O=ChriOnline, L=Tetouan, ST=Tanger, C=MA"
```

2. Exporter le certificat serveur :

```powershell
keytool -exportcert -alias payment-server `
  -keystore chrionline-server/src/main/resources/tls/payment-keystore.jks `
  -storepass changeit -rfc `
  -file chrionline-server/src/main/resources/tls/payment-server.crt
```

3. Creer le truststore client :

```powershell
New-Item -ItemType Directory -Force chrionline-client/src/main/resources/tls
keytool -importcert -alias payment-server `
  -file chrionline-server/src/main/resources/tls/payment-server.crt `
  -keystore chrionline-client/src/main/resources/tls/payment-truststore.jks `
  -storepass changeit -noprompt
```

4. Lancer le serveur paiement TLS :

```text
com.chrionline.server.network.PaymentTlsServer
```

5. Lancer le serveur principal et le client comme d'habitude.

Mode strict optionnel :

```text
-Dchrionline.payment.tls.required=true
```

Sans mode strict, si le serveur TLS n'est pas lance ou si le certificat manque,
le paiement classique continue pour ne pas casser l'application.
