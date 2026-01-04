package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Settlement with demographic data from 2021 census
 */
@Entity
@Table(name = "settlements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    // Уникален код на населеното място
    // Колона Q: ekatte
    @Id
    @Column(name = "ekatte", length = 10)
    private String ekatte;

    // Връзка с община
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_code", referencedColumnName = "municipality_code", nullable = false)
    private Municipality municipality;

    // Име на населеното място
    // Колона H: settlement_norm
    @Column(name = "settlement_norm", length = 200)
    private String settlementNorm;

    // Тип населено място: село или град
    // Колона K: village_city
    @Column(name = "village_city", length = 20)
    private String villageCity;

    // ========== ПРЕБРОЯВАНЕ 2021 - НАСЕЛЕНИЕ НА НИВО НАСЕЛЕНО МЯСТО ==========

    // Общо население на населеното място
    // Колона DU: Общо население на населено място преброяване 2021 г.
    @Column(name = "settlement_population")
    private Integer settlementPopulation;

    // Население под 15 годишна възраст
    // Колона DV: Население под 15 годишна възраст преброяване 2021 г.
    @Column(name = "population_under_15")
    private Integer populationUnder15;

    // Население 15-64 годишна възраст
    // Колона DW: Население 15-64 годишна възраст преброяване 2021 г.
    @Column(name = "population_15_64")
    private Integer population1564;

    // Население 65 и повече години
    // Колона DX: Население 65 и повеч години преброяване 2021 г.
    @Column(name = "population_over_65")
    private Integer populationOver65;

    // ========== ПРЕБРОЯВАНЕ 2021 - ОБРАЗОВАНИЕ ==========

    // Висше образование
    // Колона DY: Висше общо преброяване 2021 г.
    @Column(name = "higher_education")
    private Integer higherEducation;

    // Средно образование
    // Колона DZ: Средно общо преброяване 2021 г.
    @Column(name = "secondary_education")
    private Integer secondaryEducation;

    // Основно образование
    // Колона EA: Основно общо преброяване 2021 г.
    @Column(name = "primary_education")
    private Integer primaryEducation;

    // Начално образование
    // Колона EB: Начално общо преброяване 2021 г.
    @Column(name = "elementary_education")
    private Integer elementaryEducation;

    // Без образование
    // Колона EC: Без образование общо преброяване 2021 г.
    @Column(name = "no_education")
    private Integer noEducation;

    // Грамотни
    // Колона ED: Грамотни преброяване 2021 г.
    @Column(name = "literate")
    private Integer literate;

    // Неграмотни
    // Колона EE: Неграмотни преброяване 2021 г.
    @Column(name = "illiterate")
    private Integer illiterate;

    // Връзки
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Chitalishte> chitalishta = new ArrayList<>();

    public void addChitalishte(Chitalishte chitalishte) {
        chitalishta.add(chitalishte);
        chitalishte.setSettlement(this);
    }
}