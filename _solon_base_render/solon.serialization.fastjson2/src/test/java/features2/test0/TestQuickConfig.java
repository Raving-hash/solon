package features2.test0;

import features2.model.UserDo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.serialization.fastjson2.Fastjson2RenderFactory;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.annotation.TestPropertySource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author noear 2023/1/16 created
 */
@TestPropertySource("classpath:features2_test0.yml")
@RunWith(SolonJUnit4ClassRunner.class)
public class TestQuickConfig {
    @Inject
    Fastjson2RenderFactory renderFactory;

    @Test
    public void hello2() throws Throwable{
        UserDo userDo = new UserDo();

        Map<String, Object> data = new HashMap<>();
        data.put("time", new Date(1673861993477L));
        data.put("long", 12L);
        data.put("int", 12);
        data.put("null", null);

        userDo.setMap1(data);

        ContextEmpty ctx = new ContextEmpty();
        renderFactory.create().render(userDo, ctx);
        String output = ctx.attr("output");

        System.out.println(output);

        assert ONode.load(output).count() == 5;

        //error: 时间默认是字符串
        assert "{\"b1\":true,\"d1\":1.0,\"map1\":{\"time\":\"2023-01-16 17:39:53.477\",\"long\":12,\"int\":12},\"n1\":1,\"s1\":\"noear\"}".equals(output);
    }
}
