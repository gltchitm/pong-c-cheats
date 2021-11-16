#!/usr/bin/env python3

import gi

gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

from sys import argv

dialog = Gtk.MessageDialog(
    message_type=Gtk.MessageType.QUESTION,
    buttons=Gtk.ButtonsType.YES_NO,
    text='Pong C Cheats Web',
    secondary_text='Are you trying to use Pong C Cheats Web?'
)
dialog.set_keep_above(True)

response = dialog.run()

dialog.destroy()

if response == Gtk.ResponseType.YES:
    dialog = Gtk.MessageDialog(
        message_type=Gtk.MessageType.INFO,
        buttons=Gtk.ButtonsType.OK,
        text='Pong C Cheats Web',
        secondary_text='Use this token to attach to Pong C:'
    )
    other_label = Gtk.Label()
    other_label.set_markup(f'<span weight=\'bold\' size=\'30000\'>{argv[1]}</span>')
    other_label.set_visible(True)

    dialog.get_message_area().add(other_label)
    dialog.set_keep_above(True)
    dialog.run()

    dialog.destroy()
else:
    exit(0)
