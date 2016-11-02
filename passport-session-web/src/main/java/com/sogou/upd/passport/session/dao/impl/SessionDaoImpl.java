package com.sogou.upd.passport.session.dao.impl;

import com.sogou.upd.passport.session.dao.SessionDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by wanghuaqing on 2016/11/1.
 */
@Repository
public class SessionDaoImpl implements SessionDao {
    
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Override
    public String queryAppConfigByClientId(int clientId) {
        String sql = "SELECT server_secret FROM app_config WHERE client_id = ?";
        
        String serverSecret = jdbcTemplate.queryForObject(sql, String.class, clientId);
        
        return serverSecret;
    }
}
