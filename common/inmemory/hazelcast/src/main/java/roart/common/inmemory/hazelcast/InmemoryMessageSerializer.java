package roart.common.inmemory.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import roart.common.inmemory.model.InmemoryMessage;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

public class InmemoryMessageSerializer implements StreamSerializer<InmemoryMessage> {

  @Override
      public int getTypeId() {
      return 3;
  }

  @Override
      public void write( ObjectDataOutput out, InmemoryMessage object ) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      XMLEncoder encoder = new XMLEncoder( bos );
      encoder.writeObject( object );
      encoder.close();
      out.write( bos.toByteArray() );
      //System.out.println(bos.toString());
  }

  @Override
      public InmemoryMessage read( ObjectDataInput in ) throws IOException {
      InputStream inputStream = (InputStream) in;
      XMLDecoder decoder = new XMLDecoder( inputStream );
      return (InmemoryMessage) decoder.readObject();
  }

  @Override
      public void destroy() {
  }
}
