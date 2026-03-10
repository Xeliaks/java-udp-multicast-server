# UDP Multicast Game Event Bus – Server

Server for the multicast game event bus (arena/battle simulation). Joins the multicast group and can send and receive game-state events.

---

## How to work with it from the console

**1. Open a terminal** and go to the project folder:

```bash
cd /path/to/java-udp-multicast-server
```

**2. Start the server** (interactive mode):

```bash
mvn exec:java -Dexec.mainClass="com.game.multicast.server.MulticastGameEventServerMain"
```

You should see something like:

```
Multicast game event server started on 230.0.0.1:6789
Enter events (e.g. PLAYER_JOINED p1, SCORE_UPDATED p1 100) or 'quit' to exit.
```

**3. Type events** (one per line) and press Enter. The server sends them to the multicast group; any client on the same group will receive them.

Examples:

```
PLAYER_JOINED p1
PLAYER_MOVED p1 x=100,y=200
PLAYER_FIRED p1
SCORE_UPDATED p1 150
```

**4. Stop the server:** type `quit` or `exit` and press Enter.

**Optional:** To only listen (no typing), run in headless mode and stop with **Ctrl+C**:

```bash
mvn exec:java -Dexec.mainClass="com.game.multicast.server.MulticastGameEventServerMain" -Dserver.headless=true
```

---

## Run from the console (reference)

### Build

```bash
mvn clean compile
```

### Run the server (interactive)

Uses default multicast group `230.0.0.1` and port `6789`. You can type events on stdin (e.g. `PLAYER_JOINED p1`) or `quit` to exit.

```bash
mvn exec:java -Dexec.mainClass="com.game.multicast.server.MulticastGameEventServerMain"
```

### Run the server with custom group and port

```bash
mvn exec:java -Dexec.mainClass="com.game.multicast.server.MulticastGameEventServerMain" -Dexec.args="230.0.0.1 6789"
```

### Run the server in headless mode (listen only)

No stdin; server only receives events. Stops with Ctrl+C.

```bash
mvn exec:java -Dexec.mainClass="com.game.multicast.server.MulticastGameEventServerMain" -Dexec.args="230.0.0.1 6789" -Dserver.headless=true
```

### Run tests

```bash
mvn test
```

### Package runnable JAR

```bash
mvn package
```

Run the JAR (default group and port):

```bash
java -jar target/udp-multicast-game-server-1.0.0-SNAPSHOT.jar
```

With custom group and port:

```bash
java -jar target/udp-multicast-game-server-1.0.0-SNAPSHOT.jar 230.0.0.1 6789
```

Headless:

```bash
java -Dserver.headless=true -jar target/udp-multicast-game-server-1.0.0-SNAPSHOT.jar
```

## Datagram example (UDP send/receive)

Simple UDP sender and receiver examples (no multicast):

**Terminal 1 – start receiver** (listens on port 9099):

```bash
mvn exec:java -Dexec.mainClass="com.game.multicast.example.DatagramReceive"
# Or with custom port:
mvn exec:java -Dexec.mainClass="com.game.multicast.example.DatagramReceive" -Dexec.args="9099"
```

**Terminal 2 – send a message:**

```bash
mvn exec:java -Dexec.mainClass="com.game.multicast.example.DatagramSend" -Dexec.args="Hello"
# Or message and port:
mvn exec:java -Dexec.mainClass="com.game.multicast.example.DatagramSend" -Dexec.args="Hello 9099"
```

The receiver prints the received string and exits after one packet.

---

## Interactive input format

When running interactively, type one event per line:

- `EVENT_TYPE playerId [payload]`
- Examples:
  - `PLAYER_JOINED p1`
  - `PLAYER_MOVED p1 x=100,y=200`
  - `PLAYER_FIRED p1`
  - `PLAYER_HIT p1 targetId=p2,damage=10`
  - `PLAYER_LEFT p1`
  - `SCORE_UPDATED p1 150`
- Type `quit` or `exit` to stop the server.

---

## Deploy on AWS EC2

### 1. Launch an EC2 instance

- **AMI:** Amazon Linux 2023 or Ubuntu 22.04.
- **Instance type:** e.g. `t3.micro` or `t3.small`.
- **Security group:** allow **SSH (22)** and **UDP 6789** (custom UDP, port 6789, source: your IP or `0.0.0.0/0` if you need external clients).
- Attach a key pair and note the public IP or hostname.

### 2. Install Java 17 on the instance

**Amazon Linux 2023:**

```bash
sudo dnf install -y java-17-amazon-corretto
java -version
```

**Ubuntu 22.04:**

```bash
sudo apt update && sudo apt install -y openjdk-17-jre-headless
java -version
```

### 3. Copy the application to EC2

From your machine (build the JAR first with `mvn package`):

```bash
scp -i your-key.pem target/udp-multicast-game-server-1.0.0-SNAPSHOT.jar ec2-user@<EC2-PUBLIC-IP>:~/
```

Or clone the repo on EC2 and build there:

```bash
ssh -i your-key.pem ec2-user@<EC2-PUBLIC-IP>
git clone <your-repo-url> multicast-server && cd multicast-server
mvn -q package
```

### 4. Run the server on EC2

**Option A – Foreground (headless, stop with Ctrl+C):**

```bash
java -Dserver.headless=true -jar udp-multicast-game-server-1.0.0-SNAPSHOT.jar 230.0.0.1 6789
```

**Option B – Run script (from project root on EC2):**

```bash
chmod +x scripts/run-server.sh
./scripts/run-server.sh 230.0.0.1 6789
```

**Option C – Systemd service (runs in background, survives reboot):**

1. Copy the JAR to a fixed path, e.g. `/home/ec2-user/multicast-server/`.
2. Edit `scripts/multicast-game-server.service`: set `User`, `WorkingDirectory`, and paths in `ExecStart` to match your setup.
3. Install and start:

```bash
sudo cp scripts/multicast-game-server.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable multicast-game-server
sudo systemctl start multicast-game-server
sudo systemctl status multicast-game-server
```

View logs: `journalctl -u multicast-game-server -f`

### 5. Check that UDP is open

From your laptop (replace with your EC2 IP):

```bash
# Optional: test that the port is reachable (UDP is hard to probe; the server log will show traffic)
nc -u -v <EC2-PUBLIC-IP> 6789
```

### Note on multicast and AWS

Standard AWS VPCs do **not** support multicast between instances. So:

- The server on EC2 will join the multicast group and listen/send on that host.
- Clients that need to share the same multicast traffic must run in the **same network** (e.g. same EC2 instance, same subnet with multicast, or a network that supports multicast). For clients on the internet, you’d typically use a different architecture (e.g. TCP/WebSocket to the server, or a dedicated multicast-capable network).
