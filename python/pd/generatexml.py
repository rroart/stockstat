import datareader as dr

import xml.etree.ElementTree as ET

from datetime import datetime

marketidname = 'nasdaq'

reader = dr.Datareader()
lists = reader.my(2, 90)
market = ET.Element('market')
rows = ET.SubElement(market, 'rows')
for list in lists:
    symbol = list[0]
    frame = list[1]
    ticker = symbol[0]
    myname = symbol[2]
    for framerow in frame.itertuples():
        row = ET.SubElement(rows, 'row')
        marketid = ET.SubElement(row, 'marketid')
        marketid.text = marketidname
        date = ET.SubElement(row, 'date')
        datestr = framerow[0]
        mydate = datetime.strptime(datestr, '%Y-%M-%d')
        #print(framerow[0])
        date.text = mydate.strftime('%d.%M.%Y')
        #print(date.text)
        #date.text = str(framerow[5])
        name = ET.SubElement(row, 'id')
        name.text = ticker
        name = ET.SubElement(row, 'name')
        name.text = myname
        price = ET.SubElement(row, 'price')
        price.text = str(framerow[4])
        if price.text == 'nan':
            price.text = '-'
        pricelow = ET.SubElement(row, 'pricelow')
        pricelow.text = str(framerow[3])
        if pricelow.text == 'nan':
            pricelow.text = '-'
        pricehigh = ET.SubElement(row, 'pricehigh')
        pricehigh.text = str(framerow[2])
        if pricehigh.text == 'nan':
            pricehigh.text = '-'
        volume = ET.SubElement(row, 'volume')
        volume.text = str(framerow[5])
        if volume.text == 'nan':
            volume.text = '-'
        
#ET.dump(market)
tree =  ET.ElementTree(market)
tree.write('/tmp/nasdaq.xml')
    
#class GenerateXML:

