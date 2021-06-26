
package com.cxnb.entity;


import lombok.Data;

import java.util.List;

/**用于接收从区块链浏览器中返回的账户数据**/
@Data
public class TransRes {
    private String status;
    private String message;
    private List<TransInfo> result;
}