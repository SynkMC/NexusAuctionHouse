# NexusAuctionHouse
A simple and optimized Auction House plugin

## Requirements 
- Java 8+
- A Minecraft server above 1.7
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- [SynkLibs](https://modrinth.com/plugin/synklibs)

## Commands and permissions
- /ah - opens the Auction House menu
- /ah reload - reloads the configuration - nah.command.reload
- /ah search - Search for items in the Auction House 
- /ah sell <price> - list an item on the Auction House 
- /ah logs - opens the list of sold items - nah.menu.logs
- /ah toggle - toggles the AH on or off - nah.command.reload

## Features
- Searching
- Sorting
- Admin control
- Listing fee
- Expiry
- Logging for admins
- Item banning
- Webhook notifications
- Slots cap customization

## Developer API
The plugin now features a developer API.
The JavaDocs can be found [here](https://synkdev.cc/storage/javadocs/nah)

### Dependency information:
#### For Maven:
```xml
    <repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
    </repositories>

	<dependency>
	    <groupId>com.github.SynkMC</groupId>
	    <artifactId>NexusAuctionHouse</artifactId>
	    <version>1.5.1</version>
	    <scope>provided</scope>
	</dependency>
```
#### For Gradle:
```groovy
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}

dependencies {
	        implementation 'com.github.SynkMC:NexusAuctionHouse:1.5.1'
	}
```
