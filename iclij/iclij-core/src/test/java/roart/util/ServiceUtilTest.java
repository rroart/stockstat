package roart.util;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import roart.common.model.IncDecItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServiceUtilTest {
    @Test
    public void test()  {
        List<Boolean> list = new ArrayList<>();
        list.add(null);
        list.add(true);
        list.add(false);
        System.out.println(list);
        List<Boolean> list2 = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(list2);
        long c = list2.stream().filter(i -> i == true).count();                            
        System.out.println(c);
        long d = list2.stream().filter(i -> i).count();                            
        System.out.println(d);
    }
    @Test
    public void test2()  {
        IncDecItem i1 = new IncDecItem();
        IncDecItem i2 = new IncDecItem();
        IncDecItem i3 = new IncDecItem();
        i1.setVerified(null);
        i2.setVerified(true);
        i3.setVerified(false);
        List<IncDecItem> list = new ArrayList<>();
        list.add(i1);
        list.add(i2);
        list.add(i3);
        List<Boolean> list1 = list.stream().map(IncDecItem::getVerified).collect(Collectors.toList());
        System.out.println(list1);
        List<Boolean> list2 = list1.stream().filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(list2);
        long c = list2.stream().filter(i -> i == true).count();                            
        System.out.println(c);
        long d = list2.stream().filter(i -> i).count();                            
        System.out.println(d);
        long count = list.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).filter(i -> i == true).count();                                    
        System.out.println(count);
    }
}
