# FakeCreative

A super simple plugin that allows you to give out a real looking creative mode to players. This will send
a client-side packet to the player to change their game mode to creative, and manually recreate some of
the creative mode features to mimic expected behavior. Items are also trimmed when taken from the player's
inventory to prevent exploits.

This plugin was made because Puremin0rez bet me that this would not be possible, so boom.

You can use the following command to manage the fake creative mode:

```
/fakecreative enable|disable [player]
```

If no player is specified, the command will be executed for the sender.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.