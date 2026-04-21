package school.hei.federationagricole.repository;

import org.springframework.stereotype.Repository;
import school.hei.federationagricole.controller.dto.CreateCollectivityStructure;
import school.hei.federationagricole.entity.Collectivity;
import school.hei.federationagricole.entity.CollectivityStructure;
import school.hei.federationagricole.entity.Member;

import java.sql.*;
import java.util.UUID;

@Repository
public class CollectivityRepository {
    private final Connection connection;
    private final MemberRepository memberRepository;

    public CollectivityRepository(Connection connection, MemberRepository memberRepository) {
        this.connection = connection;
        this.memberRepository = memberRepository;
    }

    public Collectivity save(Collectivity collectivity, CreateCollectivityStructure structure) {
        String id = UUID.randomUUID().toString();
        collectivity.setId(id);

        String collectivityQuery = "INSERT INTO collectivity (id, location, federation_approval) VALUES (?, ?, ?)";
        String structureQuery = "INSERT INTO collectivity_structure (collectivity_id, president_id, vice_president_id, treasurer_id, secretary_id) VALUES (?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement collectivityStmt = connection.prepareStatement(collectivityQuery)) {
                collectivityStmt.setString(1, id);
                collectivityStmt.setString(2, collectivity.getLocation());
                collectivityStmt.setBoolean(3, collectivity.isFederationApproval());
                collectivityStmt.executeUpdate();
            }

            try (PreparedStatement structureStmt = connection.prepareStatement(structureQuery)) {
                structureStmt.setString(1, id);
                structureStmt.setString(2, structure.getPresident());
                structureStmt.setString(3, structure.getVicePresident());
                structureStmt.setString(4, structure.getTreasurer());
                structureStmt.setString(5, structure.getSecretary());
                structureStmt.executeUpdate();
            }

            // Save members association
            if (collectivity.getMembers() != null) {
                for (Member member : collectivity.getMembers()) {
                    memberRepository.saveCollectivityMembership(member.getId().getValue(), id);
                }
            }

            connection.commit();
            return findById(id);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                // Ignore
            }
            throw new RuntimeException("Error saving collectivity", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    public Collectivity findById(String id) {
        String query = "SELECT c.*, cs.president_id, cs.vice_president_id, cs.treasurer_id, cs.secretary_id " +
                "FROM collectivity c " +
                "LEFT JOIN collectivity_structure cs ON c.id = cs.collectivity_id " +
                "WHERE c.id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Collectivity collectivity = Collectivity.builder()
                        .id(resultSet.getString("id"))
                        .location(resultSet.getString("location"))
                        .federationApproval(resultSet.getBoolean("federation_approval"))
                        .build();

                // Map structure
                CollectivityStructure structure = new CollectivityStructure();
                structure.setPresident(memberRepository.findById(resultSet.getString("president_id")).orElse(null));
                structure.setVicePresident(memberRepository.findById(resultSet.getString("vice_president_id")).orElse(null));
                structure.setTreasurer(memberRepository.findById(resultSet.getString("treasurer_id")).orElse(null));
                structure.setSecretary(memberRepository.findById(resultSet.getString("secretary_id")).orElse(null));
                collectivity.setStructure(structure);

                // Map members
                collectivity.setMembers(memberRepository.findMembersByCollectivityId(id));

                return collectivity;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding collectivity by id", e);
        }
        return null;
    }
}
