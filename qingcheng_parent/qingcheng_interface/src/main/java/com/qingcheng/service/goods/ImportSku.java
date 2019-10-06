package com.qingcheng.service.goods;

import java.io.IOException;

public interface ImportSku {

    /**
     * 导入sku数据到es
     */
    void importAllSkuList() throws IOException;
}
