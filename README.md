# Hackatown Unifran — SMS alerts

MVC application that sends a flood-risk SMS alert through Twilio.

## MVC structure

- `model`: alert input and output data
- `controller`: receives the request and delegates it to the service
- `service`: validates the request, creates alert content, and sends it through Twilio
- `view`: renders the result to the console for the demo

The service accepts Brazilian phone numbers in international format: `+55` + area code + phone number, for example `+5511999999999`.

## Twilio configuration

Before starting the application, export these environment variables. Do not commit their values.

```bash
export TWILIO_ACCOUNT_SID='AC...'
export TWILIO_AUTH_TOKEN='...'
export TWILIO_FROM_NUMBER='+15551234567'
```

`TWILIO_FROM_NUMBER` deve ser um remetente habilitado para SMS na conta Twilio. Contas de teste só enviam para números de destino verificados. O SMS aceita telefones brasileiros em formato internacional, por exemplo `+5511999999999`.

Para executar apenas a demonstração de SMS após configurar as variáveis:

```bash
javac -d /tmp/hackatown-alerts $(find src/main/java -name '*.java')
java -cp /tmp/hackatown-alerts br.com.hackatown.elnino.alerts.AlertApplication
```
