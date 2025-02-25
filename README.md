
Instructions for Testing and Running the Project

📥 Step 1: Extract the ZIP File
After receiving the newswhipproject.zip file, extract it:
unzip newswhipproject.zip
cd newswhipproject


🛠 Step 2: Install Dependencies (If Not Installed)
Ensure the following are installed:
Java (JDK 8 or later)
Scala (2.13)
SBT (Scala Build Tool)
MacOS (via Homebrew)

/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

brew install openjdk scala sbt


🏗 Step 3: Compile the Project
Navigate to the extracted project folder and compile:
sbt compile


✅ Step 4: Run Unit Tests
Run all the tests to ensure everything works:
sbt test


🚀 Step 5: Package as a JAR File
To create a fat JAR (standalone runnable JAR):
sbt clean compile assembly

This will generate a JAR file in the target/scala-2.13/ directory.

▶️ Step 6: Run the Application
Run the generated JAR file:
java -jar target/scala-2.13/newswhipproject-assembly.jar
Alternatively, they can use SBT to run it directly:
sbt run


🎯 Usage Example
Once running, they can enter commands:
Welcome to the URL Social Score Manager. Enter commands: ADD <url> <score>, REMOVE <url>, EXPORT, SAVE, EXIT
> ADD http://www.example.com 10
Added: http://www.example.com with score 10
> EXPORT
domain;urls;social_score
example.com;1;10


📞 Contact
For any issues, contact Bryan O'Brien at: 📧 Email: bryananthonyobrien@yahoo.co.uk

🎉 You’re Done!
If you followed all steps correctly, the project should compile, run tests successfully, and execute properly. 🚀


