RabbitMQ / CouchDB / Email Application

You need to install RabbitMQ before use this app
you can specify sender email and couchdb user
-encrypt <your email account password> will give you encrypted value (without <>)
in order to specify sender email
in config.ini add [sender_email]\\n<your email account> (without <>)
[sender_email_encrypted_pass]\\n<encrypted password> (without <>)
or [sender_email_raw_pass]\\n<not ecrypted password> (without <>)
in order to specify couchdb user
in config.ini add [couchdb_user]\\n<your couchdb user name> (without <>)
[couchdb_user_encrypted_pass]\\n<encrypted password> (without <>)
or [couchdb_user_raw_pass]\\n<not ecrypted password> (without <>)