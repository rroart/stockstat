package roart.common.model;

import java.util.Objects;

public class FileObject {
    public FileObject() {
    }
public FileObject(Location fs, String file) {
		this.object = file;
		this.location = fs;
	}
public String object;
public Location location;

@Override
public String toString() {
    return location.toString() + ":" + object;
}

@Override
public boolean equals(Object o) {
    if (o == null) {
        return false;
    }
    
    if (o == this) {
        return true;
    }

    if (!(o instanceof FileObject)) {
        return false;
    }        
    
    FileObject ob = (FileObject) o;
    
    return Objects.equals(object, ob.object)
            && Objects.equals(location, ob.location);
}

@Override
public int hashCode() {
    return Objects.hash(object, location);
}
}
