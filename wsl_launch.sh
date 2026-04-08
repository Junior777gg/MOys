#Removes JOGL error from the console if launching from WSL.
cd out/artifacts/MOys_system_main_jar
java --add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.java2d=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -cp ".:*" MainSystemServiceKt
