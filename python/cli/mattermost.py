import os

from mattermostdriver import Driver

import json 

def post(message):
  url = os.environ.get('MYMATTERMOSTURL')
  token = os.environ.get('MYMATTERMOSTTOKEN')
  scheme = os.environ.get('MYMATTERMOSTSCHEME')
  port = os.environ.get('MYMATTERMOSTPORT')
  team = os.environ.get('MYMATTERMOSTTEAM')
  channel_name = os.environ.get('MYMATTERMOSTCHANNEL')
  print("py", url, token, scheme, port, team,  channel_name)

  mm = Driver({
    'url': url,
    "token": token,
    'scheme': scheme,
    'port': int(port)
  })
  mm.login()
  channel=mm.channels.get_channel_by_name_and_team_name(team, channel_name)
  channel_id=channel['id']
  mm.posts.create_post(options={
    'channel_id': channel_id,
    'message': message
  })
