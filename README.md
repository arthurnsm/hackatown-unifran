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

`TWILIO_FROM_NUMBER` must be an SMS-capable sender owned by the Twilio account. Then compile and run:

```bash
build_dir=$(mktemp -d)
javac -d "$build_dir" $(find src/main/java -name '*.java')
java -cp "$build_dir" br.com.hackatown.alerts.AlertApplication
```

The console prints the Twilio Message SID once Twilio accepts the request for delivery. A trial account can send only to verified recipient numbers.
