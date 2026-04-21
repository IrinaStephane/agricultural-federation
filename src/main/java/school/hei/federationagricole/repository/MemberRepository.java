package school.hei.federationagricole.repository;

import school.hei.federationagricole.entity.Member;

import java.sql.Connection;
import java.util.List;

public class MemberRepository {
    Connection connection;

    public MemberRepository(Connection connection) {
        this.connection = connection;
    }
}
