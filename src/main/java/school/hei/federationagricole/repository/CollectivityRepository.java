package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.Collectivity;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.Structure;
import school.hei.federationagricole.entity.enums.CollectivityOccupation;
import school.hei.federationagricole.entity.enums.Gender;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository
@AllArgsConstructor
public class CollectivityRepository {

    private final Connection connection;

    public Collectivity save(Collectivity collectivity, List<String> memberIds,
                             String presidentId, String vicePresidentId,
                             String treasurerId, String secretaryId) {

        String insertCollectivitySql = """
            INSERT INTO collectivity
                (id, number, name, speciality, federation_approval,
                 authorization_date, location, id_federation, creation_datetime)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'fed-1', NOW())
            """;

        String insertMemberSql = """
            INSERT INTO member_collectivity (id, id_member, id_collectivity, occupation, start_date)
            VALUES (?, ?, ?, ?::collectivity_occupation, ?)
            """;

        try {
            connection.setAutoCommit(false);

            // Générer un ID string unique pour la collectivité
            String collectivityId = "col-" + UUID.randomUUID().toString().substring(0, 8);

            try (PreparedStatement stmt = connection.prepareStatement(insertCollectivitySql)) {
                stmt.setString(1, collectivityId);
                stmt.setString(2, collectivity.getNumber());
                stmt.setString(3, collectivity.getName());
                stmt.setString(4, collectivity.getSpeciality());
                stmt.setBoolean(5, collectivity.isFederationApproval());
                if (collectivity.getAuthorizationDate() != null)
                    stmt.setTimestamp(6, Timestamp.from(collectivity.getAuthorizationDate()));
                else
                    stmt.setNull(6, Types.TIMESTAMP);
                stmt.setString(7, collectivity.getLocation());
                stmt.executeUpdate();
            }

            try (PreparedStatement memberStmt = connection.prepareStatement(insertMemberSql)) {
                Timestamp now = Timestamp.from(Instant.now());
                for (String memberId : memberIds) {
                    String occ = determineOccupation(memberId, presidentId,
                            vicePresidentId, treasurerId, secretaryId);
                    String mcId = "mc-" + UUID.randomUUID().toString().substring(0, 8);
                    memberStmt.setString(1, mcId);
                    memberStmt.setString(2, memberId);
                    memberStmt.setString(3, collectivityId);
                    memberStmt.setString(4, occ);
                    memberStmt.setTimestamp(5, now);
                    memberStmt.addBatch();
                }
                memberStmt.executeBatch();
            }

            connection.commit();
            return findById(collectivityId);

        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Failed to save collectivity: " + e.getMessage(), e);
        } finally {
            resetAutoCommit();
        }
    }

    public List<Collectivity> saveAll(List<Collectivity> collectivities,
                                      List<List<String>> memberIdsList,
                                      List<String> presidentIds,
                                      List<String> vicePresidentIds,
                                      List<String> treasurerIds,
                                      List<String> secretaryIds) {
        List<Collectivity> saved = new ArrayList<>();
        for (int i = 0; i < collectivities.size(); i++) {
            saved.add(save(collectivities.get(i), memberIdsList.get(i),
                    presidentIds.get(i), vicePresidentIds.get(i),
                    treasurerIds.get(i), secretaryIds.get(i)));
        }
        return saved;
    }

    public boolean existsByNumber(String number) {
        return existsBy("number", number);
    }

    public boolean existsByName(String name) {
        return existsBy("name", name);
    }

    public Collectivity assignIdentification(String id, String number, String name) {
        String sql = """
            UPDATE collectivity SET number = ?, name = ?
            WHERE id = ? AND number IS NULL AND name IS NULL
            """;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, number);
                stmt.setString(2, name);
                stmt.setString(3, id);
                if (stmt.executeUpdate() == 0) {
                    connection.rollback();
                    throw new RuntimeException("Could not assign: not found or already identified.");
                }
            }
            connection.commit();
            return findById(id);
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Failed to assign identification", e);
        } finally {
            resetAutoCommit();
        }
    }

    public Collectivity findById(String id) {
        String sql = """
            SELECT id, number, name, speciality, creation_datetime,
                   federation_approval, authorization_date, location
            FROM collectivity WHERE id = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            Collectivity c = Collectivity.builder()
                    .id(rs.getString("id"))
                    .number(rs.getString("number"))
                    .name(rs.getString("name"))
                    .speciality(rs.getString("speciality"))
                    .creationDatetime(rs.getTimestamp("creation_datetime").toInstant())
                    .federationApproval(rs.getBoolean("federation_approval"))
                    .authorizationDate(rs.getTimestamp("authorization_date") != null
                            ? rs.getTimestamp("authorization_date").toInstant() : null)
                    .location(rs.getString("location"))
                    .build();

            fetchMembersAndStructure(c);
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity", e);
        }
    }

    private boolean existsBy(String column, String value) {
        String sql = "SELECT 1 FROM collectivity WHERE " + column + " = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, value);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check collectivity uniqueness for " + column, e);
        }
    }

    private void fetchMembersAndStructure(Collectivity collectivity) {
        String sql = """
            SELECT m.id, m.first_name, m.last_name, m.birth_date, m.enrolment_date,
                   m.address, m.email, m.phone_number, m.profession, m.gender, mc.occupation
            FROM member_collectivity mc
            JOIN member m ON mc.id_member = m.id
            WHERE mc.id_collectivity = ? AND mc.end_date IS NULL
            """;

        List<Member> members = new ArrayList<>();
        Structure structure  = Structure.builder().build();
        Map<String, Member> cache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivity.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                final ResultSet finalRs = rs;
                String memberId = rs.getString("id");
                Member member = cache.computeIfAbsent(memberId, mid -> {
                    try {
                        return Member.builder()
                                .id(mid)
                                .firstName(finalRs.getString("first_name"))
                                .lastName(finalRs.getString("last_name"))
                                .birthDate(finalRs.getDate("birth_date").toLocalDate())
                                .enrolmentDate(finalRs.getTimestamp("enrolment_date").toInstant())
                                .address(finalRs.getString("address"))
                                .email(finalRs.getString("email"))
                                .phoneNumber(finalRs.getString("phone_number"))
                                .profession(finalRs.getString("profession"))
                                .gender(Gender.valueOf(finalRs.getString("gender")))
                                .build();
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to map member row", e);
                    }
                });

                members.add(member);

                switch (CollectivityOccupation.valueOf(rs.getString("occupation"))) {
                    case PRESIDENT      -> structure.setPresident(member);
                    case VICE_PRESIDENT -> structure.setVicePresident(member);
                    case TREASURER      -> structure.setTreasurer(member);
                    case SECRETARY      -> structure.setSecretary(member);
                    default             -> { /* SENIOR / JUNIOR not in structure */ }
                }
            }
            collectivity.setMembers(members);
            collectivity.setStructure(structure);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch members and structure", e);
        }
    }

    private String determineOccupation(String memberId, String presidentId,
                                       String vicePresidentId, String treasurerId,
                                       String secretaryId) {
        if (memberId.equals(presidentId))     return "PRESIDENT";
        if (memberId.equals(vicePresidentId)) return "VICE_PRESIDENT";
        if (memberId.equals(treasurerId))     return "TREASURER";
        if (memberId.equals(secretaryId))     return "SECRETARY";
        return hasMinimumSeniority(memberId) ? "SENIOR" : "JUNIOR";
    }

    private boolean hasMinimumSeniority(String memberId) {
        String sql = "SELECT enrolment_date FROM member WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long months = ChronoUnit.MONTHS.between(
                        rs.getTimestamp("enrolment_date").toLocalDateTime(), LocalDateTime.now());
                return months >= 6;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check seniority", e);
        }
    }

    private void rollback() {
        try { connection.rollback(); }
        catch (SQLException ex) { throw new RuntimeException("Rollback failed", ex); }
    }

    private void resetAutoCommit() {
        try { connection.setAutoCommit(true); }
        catch (SQLException e) { throw new RuntimeException("Failed to reset auto-commit", e); }
    }
}