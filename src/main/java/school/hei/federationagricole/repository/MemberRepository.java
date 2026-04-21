package school.hei.federationagricole.repository;

import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.MemberIdentifier;
import school.hei.federationagricole.entity.GenderEnum;
import school.hei.federationagricole.entity.MemberOccupationEnum;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {
    private final Connection connection;

    public MemberRepository(Connection connection) {
        this.connection = connection;
    }

    public Member save(Member member) {
        String query = "INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation, registration_fee_paid, membership_dues_paid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name, birth_date = EXCLUDED.birth_date, " +
                "gender = EXCLUDED.gender, address = EXCLUDED.address, profession = EXCLUDED.profession, phone_number = EXCLUDED.phone_number, " +
                "email = EXCLUDED.email, occupation = EXCLUDED.occupation, registration_fee_paid = EXCLUDED.registration_fee_paid, membership_dues_paid = EXCLUDED.membership_dues_paid " +
                "RETURNING *";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, member.getId().getValue());
            statement.setString(2, member.getFirstName());
            statement.setString(3, member.getLastName());
            statement.setObject(4, member.getBirthDate());
            statement.setString(5, member.getGender().name());
            statement.setString(6, member.getAddress());
            statement.setString(7, member.getProfession());
            statement.setString(8, member.getPhoneNumber());
            statement.setString(9, member.getEmail());
            statement.setString(10, member.getOccupation().name());
            statement.setBoolean(11, member.isRegistrationFeePaid());
            statement.setBoolean(12, member.isMembershipDuesPaid());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToMember(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving member", e);
        }
        return null;
    }

    public void saveReferees(String memberId, List<String> refereeIds) {
        String query = "INSERT INTO member_referee (member_id, referee_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String refereeId : refereeIds) {
                statement.setString(1, memberId);
                statement.setString(2, refereeId);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving referees", e);
        }
    }

    public void saveCollectivityMembership(String memberId, String collectivityId) {
        String query = "INSERT INTO collectivity_member (member_id, collectivity_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, memberId);
            statement.setString(2, collectivityId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving collectivity membership", e);
        }
    }

    public Optional<Member> findById(String id) {
        String query = "SELECT * FROM member WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Member member = mapResultSetToMember(resultSet);
                member.setReferees(findRefereesByMemberId(id));
                return Optional.of(member);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding member by id", e);
        }
        return Optional.empty();
    }

    public List<Member> findAllById(List<String> ids) {
        List<Member> members = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return members;

        StringBuilder query = new StringBuilder("SELECT * FROM member WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            query.append("?");
            if (i < ids.size() - 1) query.append(", ");
        }
        query.append(")");

        try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                statement.setString(i + 1, ids.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                members.add(mapResultSetToMember(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding members by ids", e);
        }
        return members;
    }

    public List<Member> findRefereesByMemberId(String memberId) {
        List<Member> referees = new ArrayList<>();
        String query = "SELECT m.* FROM member m JOIN member_referee mr ON m.id = mr.referee_id WHERE mr.member_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, memberId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                referees.add(mapResultSetToMember(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding referees", e);
        }
        return referees;
    }

    public List<Member> findMembersByCollectivityId(String collectivityId) {
        List<Member> members = new ArrayList<>();
        String query = "SELECT m.* FROM member m JOIN collectivity_member cm ON m.id = cm.member_id WHERE cm.collectivity_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, collectivityId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                members.add(mapResultSetToMember(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding members by collectivity id", e);
        }
        return members;
    }

    private Member mapResultSetToMember(ResultSet resultSet) throws SQLException {
        return Member.builder()
                .id(new MemberIdentifier(resultSet.getString("id")))
                .firstName(resultSet.getString("first_name"))
                .lastName(resultSet.getString("last_name"))
                .birthDate(resultSet.getObject("birth_date", LocalDate.class))
                .gender(GenderEnum.valueOf(resultSet.getString("gender")))
                .address(resultSet.getString("address"))
                .profession(resultSet.getString("profession"))
                .phoneNumber(resultSet.getString("phone_number"))
                .email(resultSet.getString("email"))
                .occupation(MemberOccupationEnum.valueOf(resultSet.getString("occupation")))
                .registrationFeePaid(resultSet.getBoolean("registration_fee_paid"))
                .membershipDuesPaid(resultSet.getBoolean("membership_dues_paid"))
                .build();
    }
}
