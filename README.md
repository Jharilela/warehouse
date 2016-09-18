# warehouse
Java DB Management system build for clothing retailers

This project uses apache derby database to store data.
The derby database is located in `/warehouseDb`

This database has to be first improted into the project folder by restoring the backup by exucting the OS cli command
`jdbc:derby:sample;restoreFrom=c:\mybackups\sample`

Other methods of restoring can be found in the [apache derby guide](http://db.apache.org/derby/docs/10.9/adminguide/cadminparttwo.html)

Once the databse has been recreated and connection has been established, we can execute the jar file located in `/dist/warehouse.jar`

The password of the admin is `password` by default. Otherwise it can be found in the `/password.rtf` file
