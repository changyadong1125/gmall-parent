package activity.impl;



import activity.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.item.client.impl
 * class:ItemDegradeFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-18:27
 * @Version: v1.0
 * @Description:
 */
@Component
public class CartDegradeFeignClient implements CartFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取用户选中列表
     */
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        return null;
    }
}
