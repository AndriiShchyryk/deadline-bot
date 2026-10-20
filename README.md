# Deadline Bot

A Telegram bot that helps students keep track of their university deadlines.

## Features

- `/add` — add a new deadline
- `/list` — see all your deadlines, sorted by date
- `/ping` — check that the bot is running

## Example

```
/add CS301 Essay 25.10.2026 23:59
→ Added: Essay

/list
→ Your deadlines:
  CS301 — Essay — 25.10.2026 23:59

/add CS301 Essay 32.13.2026 23:59
→ Invalid date or time. Use dd.MM.yyyy HH:mm, e.g. 20.10.2026 23:59
```

## Tech stack

- Java 21
- Maven
- TelegramBots 10.3.0


## Project structure

- `Main` — reads the bot token from an environment variable and starts the bot.
- `DeadlineBot` — receives messages from Telegram, decides which command was sent and sends the reply.
- `DeadlineParser` — checks the format of the `/add` command and turns the text into a `Deadline`.
- `Deadline` — a model of one deadline: course, title and due date.
- `DeadlineStore` — keeps deadlines in memory, separately for each chat, and returns them sorted by date.

Each class has one responsibility, so the parsing and storage logic can be changed or tested without touching the Telegram code.


## How to run

1. Create a bot with [@BotFather](https://t.me/BotFather) in Telegram (`/newbot`) and copy the token it gives you.
2. Clone this repository and open it in IntelliJ IDEA (Java 21 is required).
3. Set the token as an environment variable `BOT_TOKEN`
   (Run → Edit Configurations → Environment variables: `BOT_TOKEN=your_token`).
   The token is never stored in the code.
4. Run `Main` and send `/ping` to your bot. It should reply `pong`.


## Roadmap

- [ ] Store deadlines in a database so they are not lost after a restart
- [ ] Send reminders one day and one hour before a deadline
- [ ] Add `/start`, `/help` and `/delete` commands
- [ ] Replace `printStackTrace` with proper logging
- [ ] Deploy the bot to a server so it runs 24/7