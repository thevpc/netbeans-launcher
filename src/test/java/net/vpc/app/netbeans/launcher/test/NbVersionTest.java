package net.vpc.app.netbeans.launcher.test;

import net.vpc.app.netbeans.launcher.NetbeansConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NbVersionTest {
    @Test
    public void testNbVersion(){
        Assertions.assertEquals(
                "12.0",
                NetbeansConfigService.parseVersionData("12.0-631bd69cd6112b1cc4c892c24e3e605b1ba04241",null).getVersion()
        );
        Assertions.assertEquals(
                "11.3",
                NetbeansConfigService.parseVersionData("11.3-6b879cb782eaa4f13a731aff82eada11289a66f7",null).getVersion()
        );
        Assertions.assertEquals(
                "11.2",
                NetbeansConfigService.parseVersionData("11.2-d04fb24027334c4b6fd8397b5d0cdd33187a8f54",null).getVersion()
        );
        Assertions.assertEquals(
                "11.1",
                NetbeansConfigService.parseVersionData("netbeans-release-428-on-20190716",null).getVersion()
        );
        Assertions.assertEquals(
                "11.0",
                NetbeansConfigService.parseVersionData("incubator-netbeans-release-404-on-20190319",null).getVersion()
        );
        Assertions.assertEquals(
                "10.0",
                NetbeansConfigService.parseVersionData("incubator-netbeans-release-380-on-20181217",null).getVersion()
        );
        Assertions.assertEquals(
                "9.0",
                NetbeansConfigService.parseVersionData("incubator-netbeans-release-334-on-20180708",null).getVersion()
        );
    }
}
