package activity;


import activity.impl.CartDegradeFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.item.client.impl
 * class:ItemFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-18:27
 * @Version: v1.0
 * @Description:
 */
@FeignClient(value = "service-cart",path = "/api/cart",fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取选中购物车列表
     */
    @GetMapping("/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId);
}
