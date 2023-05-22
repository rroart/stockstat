import os

import mattermost

mymattermost = os.environ.get('MYMATTERMOST')

def post(message):
  if not mymattermost is None:
    mattermost.post(message)
