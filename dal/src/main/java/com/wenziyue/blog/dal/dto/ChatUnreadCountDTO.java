package com.wenziyue.blog.dal.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wenziyue
 */
@Data
public class ChatUnreadCountDTO implements Serializable {

    private static final long serialVersionUID = 6085487815598460308L;

    private List<Long> sessionIdList;
}
