package edu.tuberlin.dima.textmining.jedi.core.features;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Doubles;
import edu.tuberlin.dima.textmining.jedi.core.model.Edge;
import edu.tuberlin.dima.textmining.jedi.core.model.Graph;
import edu.tuberlin.dima.textmining.jedi.core.model.Solution;
import edu.tuberlin.dima.textmining.jedi.core.util.PrintCollector;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ConstraintSolverTest {

    static final Logger LOG = LoggerFactory.getLogger(ConstraintSolverTest.class);

    @Test
    public void testSolve() throws Exception {

        ConstraintSolver.ConstraintSolverBuilder<String> solverBuilder = new ConstraintSolver.ConstraintSolverBuilder<>();


        solverBuilder.add("E1", "E2", "x -> y", "1", "A", "D", 1, 1, 1);
        solverBuilder.add("E1", "E2", "x -> y", "2", "A#", "D", 3, 1, 1);
        solverBuilder.add("E2", "E3", "x -> y", "3", "D", "B", 1, 1, 1);
        solverBuilder.add("E1", "E3", "x -> y", "4", "A", "B", 1, 1, 1);


        solverBuilder.add("E3", "E4", "x -> y", "5", "B", "C", 1, 1, 1);
        PrintCollector printCollector = new PrintCollector(true);

        final List<Solution<String>> solve = solverBuilder.build().solve(printCollector);

        assertNotNull(solve);
        assertThat(solve.size(), is(4));

        assertThat(solve.get(0).getEdge(), is(new Edge(1, "1", "x -> y", "A", "D", 1, 1, 1, false)));
        assertThat(solve.get(1).getEdge(), is(new Edge(4, "4", "x -> y", "A", "B", 1, 1, 1, false)));
        assertThat(solve.get(2).getEdge(), is(new Edge(3, "3", "x -> y", "D", "B", 1, 1, 1, false)));
        assertThat(solve.get(3).getEdge(), is(new Edge(5, "5", "x -> y", "B", "C", 1, 1, 1, false)));

        printCollector.print(Joiner.on("\n").join(solve));

        printCollector.print(Graph.transform(solve).toString());
    }

    @Test
    public void testComplex() throws Exception {

        String[][] data = {
                {"Rıza", "Galatasaray High School", "ns:people.person.education..education.education.institution", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:education.educational_institution", "0.923806"},
                {"Rıza", "Galatasaray High School", "ns:people.person.education..education.education.institution", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:organization.organization", "0.831425"},
                {"Rıza", "Galatasaray High School", "ns:people.person.education..education.education.institution", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:business.employer", "0.831425"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.023784"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.country", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:organization.organization", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.country", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:organization.organization", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.country", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:organization.organization", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.country", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:organization.organization", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.021406"},
                {"Rıza", "Galatasaray High School", "ns:people.person.employment_history..business.employment_tenure.company", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:business.employer", "0.019575"},
                {"Rıza", "France", "ns:people.person.place_of_birth", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.location", "0.242735"},
                {"Rıza", "France", "ns:people.person.place_of_birth", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.dated_location", "0.218462"},
                {"Rıza", "France", "ns:people.person.place_of_birth", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.statistical_region", "0.218462"},
                {"Rıza", "France", "ns:people.person.place_of_birth", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.country", "0.218462"},
                {"Rıza", "France", "ns:people.person.place_of_birth", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:organization.organization", "0.218462"},
                {"Rıza", "France", "ns:people.person.places_lived..people.place_lived.location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.location", "0.177778"},
                {"Rıza", "France", "ns:people.person.places_lived..people.place_lived.location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.dated_location", "0.160000"},
                {"Rıza", "France", "ns:people.person.places_lived..people.place_lived.location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.statistical_region", "0.160000"},
                {"Rıza", "France", "ns:people.person.places_lived..people.place_lived.location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.country", "0.160000"},
                {"Rıza", "France", "ns:people.person.places_lived..people.place_lived.location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:organization.organization", "0.160000"},
                {"Rıza", "France", "ns:people.person.nationality", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.country", "0.135043"},
                {"Rıza", "France", "ns:people.person.nationality", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.statistical_region", "0.121538"},
                {"Rıza", "France", "ns:people.person.nationality", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.dated_location", "0.121538"},
                {"Rıza", "France", "ns:people.person.nationality", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.location", "0.121538"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.location", "0.105983"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.dated_location", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.statistical_region", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.country", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:organization.organization", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.location", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.dated_location", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.statistical_region", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:location.country", "0.095385"},
                {"Rıza", "France", "ns:people.deceased_person.place_of_death", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:organization.organization", "0.095385"},
                {"Rıza", "France", "ns:people.person.education..education.education.institution", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:education.educational_institution", "0.054701"},
                {"Rıza", "France", "ns:people.person.education..education.education.institution", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:organization.organization", "0.049231"},
                {"Rıza", "France", "ns:people.person.education..education.education.institution", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:business.employer", "0.049231"},
                {"Rıza", "France", "ns:military.military_combatant.military_conflicts..military.military_combatant_group.combatants", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:military.military_combatant", "ns:military.military_combatant", "0.042735"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.027350"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.country", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:organization.organization", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.country", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:organization.organization", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.country", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:organization.organization", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.country", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:organization.organization", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.024615"},
                {"Rıza", "France", "ns:location.location.contains", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.024615"},
                {"Rıza", "France", "ns:location.statistical_region.places_imported_from..location.imports_and_exports.imported_from", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.020513"},
                {"Rıza", "France", "ns:location.statistical_region.places_imported_from..location.imports_and_exports.imported_from", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.018462"},
                {"Rıza", "France", "ns:location.statistical_region.places_imported_from..location.imports_and_exports.imported_from", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.018462"},
                {"Rıza", "France", "ns:location.statistical_region.places_imported_from..location.imports_and_exports.imported_from", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.020513"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.country", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:organization.organization", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.country", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:organization.organization", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.country", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:organization.organization", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.country", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:organization.organization", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.018462"},
                {"Rıza", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.018462"},
                {"Rıza", "France", "ns:location.statistical_region.places_exported_to..location.imports_and_exports.exported_to", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.018803"},
                {"Rıza", "France", "ns:location.statistical_region.places_exported_to..location.imports_and_exports.exported_to", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.016923"},
                {"Rıza", "France", "ns:location.statistical_region.places_exported_to..location.imports_and_exports.exported_to", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.016923"},
                {"Rıza", "France", "ns:location.statistical_region.places_exported_to..location.imports_and_exports.exported_to", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.016923"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.013675"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.country", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:organization.organization", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.country", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:organization.organization", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.country", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:organization.organization", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.country", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:organization.organization", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.012308"},
                {"Rıza", "France", "ns:location.location.containedby", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.012308"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.administrative_division", "0.011966"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.administrative_division", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.administrative_division", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.administrative_division", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.010769"},
                {"Rıza", "France", "ns:location.country.administrative_divisions", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.010769"},
                {"Rıza", "France", "ns:music.artist.origin", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:music.artist", "ns:location.location", "0.010256"},
                {"Rıza", "France", "ns:music.artist.origin", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:music.artist", "ns:location.dated_location", "0.009231"},
                {"Rıza", "France", "ns:music.artist.origin", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:music.artist", "ns:location.statistical_region", "0.009231"},
                {"Rıza", "France", "ns:music.artist.origin", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:music.artist", "ns:location.country", "0.009231"},
                {"Rıza", "France", "ns:music.artist.origin", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:music.artist", "ns:organization.organization", "0.009231"},
                {"Rıza", "France", "ns:book.author.works_written", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:book.author", "ns:book.written_work", "0.008547"},
                {"Rıza", "France", "ns:book.author.works_written", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:people.person", "ns:book.written_work", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.citytown", "0.008547"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.citytown", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.citytown", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.citytown", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.007692"},
                {"Rıza", "France", "ns:location.country.capital", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.007692"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:base.schemastaging.organization_extra", "ns:location.location", "0.006838"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:base.schemastaging.organization_extra", "ns:location.dated_location", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:base.schemastaging.organization_extra", "ns:location.statistical_region", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:base.schemastaging.organization_extra", "ns:location.country", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:base.schemastaging.organization_extra", "ns:organization.organization", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.006154"},
                {"Rıza", "France", "ns:base.schemastaging.organization_extra.phone_number..base.schemastaging.phone_sandbox.service_location", "[X] study in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.006154"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.238318"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.country", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:organization.organization", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.country", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:organization.organization", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.country", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:organization.organization", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.country", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:organization.organization", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.214486"},
                {"agriculture", "France", "ns:location.location.contains", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.214486"},
                {"agriculture", "France", "ns:language.human_language.countries_spoken_in", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.country", "0.090343"},
                {"agriculture", "France", "ns:language.human_language.countries_spoken_in", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.statistical_region", "0.081308"},
                {"agriculture", "France", "ns:language.human_language.countries_spoken_in", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.dated_location", "0.081308"},
                {"agriculture", "France", "ns:language.human_language.countries_spoken_in", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.location", "0.081308"},
                {"agriculture", "France", "ns:education.educational_degree.people_with_this_degree..education.education.major_field_of_study", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.educational_degree", "ns:education.field_of_study", "0.066978"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.administrative_division", "0.063863"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.administrative_division", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.administrative_division", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.administrative_division", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.057477"},
                {"agriculture", "France", "ns:location.country.administrative_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.057477"},
                {"agriculture", "France", "ns:base.aareas.schema.administrative_area.administrative_children", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:base.aareas.schema.administrative_area", "ns:base.aareas.schema.administrative_area", "0.048287"},
                {"agriculture", "France", "ns:base.aareas.schema.administrative_area.administrative_children", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:base.aareas.schema.administrative_area", "ns:location.location", "0.043458"},
                {"agriculture", "France", "ns:base.aareas.schema.administrative_area.administrative_children", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:base.aareas.schema.administrative_area", "0.043458"},
                {"agriculture", "France", "ns:base.aareas.schema.administrative_area.administrative_children", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.043458"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.043614"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.country", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:organization.organization", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.country", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:organization.organization", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.country", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:organization.organization", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.country", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:organization.organization", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.039252"},
                {"agriculture", "France", "ns:location.location.containedby", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.039252"},
                {"agriculture", "France", "ns:language.human_language.main_country", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.country", "0.038941"},
                {"agriculture", "France", "ns:language.human_language.main_country", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.statistical_region", "0.035047"},
                {"agriculture", "France", "ns:language.human_language.main_country", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.dated_location", "0.035047"},
                {"agriculture", "France", "ns:language.human_language.main_country", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:language.human_language", "ns:location.location", "0.035047"},
                {"agriculture", "France", "ns:education.field_of_study.students_majoring..education.education.institution", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.field_of_study", "ns:education.educational_institution", "0.038941"},
                {"agriculture", "France", "ns:education.field_of_study.students_majoring..education.education.institution", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.field_of_study", "ns:organization.organization", "0.035047"},
                {"agriculture", "France", "ns:education.field_of_study.students_majoring..education.education.institution", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.field_of_study", "ns:business.employer", "0.035047"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.citytown", "0.034268"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.citytown", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.citytown", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.citytown", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.030841"},
                {"agriculture", "France", "ns:location.country.capital", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.030841"},
                {"agriculture", "France", "ns:military.military_combatant.military_conflicts..military.military_combatant_group.combatants", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:military.military_combatant", "ns:military.military_combatant", "0.026480"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.023364"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.country", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:organization.organization", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.country", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:organization.organization", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.country", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:organization.organization", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.country", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:organization.organization", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.021028"},
                {"agriculture", "France", "ns:location.location.adjoin_s..location.adjoining_relationship.adjoins", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.021028"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.administrative_division", "0.021807"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.administrative_division", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.administrative_division", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.administrative_division", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.019626"},
                {"agriculture", "France", "ns:location.country.first_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.019626"},
                {"agriculture", "France", "ns:people.person.nationality", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.country", "0.017134"},
                {"agriculture", "France", "ns:people.person.nationality", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.statistical_region", "0.015421"},
                {"agriculture", "France", "ns:people.person.nationality", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.dated_location", "0.015421"},
                {"agriculture", "France", "ns:people.person.nationality", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.location", "0.015421"},
                {"agriculture", "France", "ns:martial_arts.martial_art.origin", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:martial_arts.martial_art", "ns:location.location", "0.017134"},
                {"agriculture", "France", "ns:martial_arts.martial_art.origin", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:martial_arts.martial_art", "ns:location.dated_location", "0.015421"},
                {"agriculture", "France", "ns:martial_arts.martial_art.origin", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:martial_arts.martial_art", "ns:location.statistical_region", "0.015421"},
                {"agriculture", "France", "ns:martial_arts.martial_art.origin", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:martial_arts.martial_art", "ns:location.country", "0.015421"},
                {"agriculture", "France", "ns:martial_arts.martial_art.origin", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:martial_arts.martial_art", "ns:organization.organization", "0.015421"},
                {"agriculture", "France", "ns:education.educational_degree.people_with_this_degree..education.education.specialization", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.educational_degree", "ns:education.field_of_study", "0.015576"},
                {"agriculture", "France", "ns:education.educational_degree.people_with_this_degree..education.education.minor", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.educational_degree", "ns:education.field_of_study", "0.014019"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.citytown", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.012461"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.citytown", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.011215"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.citytown", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.011215"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.citytown", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.country", "0.011215"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.citytown", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:organization.organization", "0.011215"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.administrative_division", "0.010903"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.statistical_region", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.dated_location", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.country", "ns:location.location", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.administrative_division", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.statistical_region", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.dated_location", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.statistical_region", "ns:location.location", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.administrative_division", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.statistical_region", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.dated_location", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.dated_location", "ns:location.location", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.administrative_division", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.statistical_region", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.dated_location", "0.009813"},
                {"agriculture", "France", "ns:location.country.second_level_divisions", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:location.location", "ns:location.location", "0.009813"},
                {"agriculture", "France", "ns:education.educational_degree.people_with_this_degree..education.education.institution", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.educational_degree", "ns:education.educational_institution", "0.009346"},
                {"agriculture", "France", "ns:education.educational_degree.people_with_this_degree..education.education.institution", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.educational_degree", "ns:organization.organization", "0.008411"},
                {"agriculture", "France", "ns:education.educational_degree.people_with_this_degree..education.education.institution", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:education.educational_degree", "ns:business.employer", "0.008411"},
                {"agriculture", "France", "ns:olympics.olympic_sport.athletes..olympics.olympic_athlete_affiliation.country", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:olympics.olympic_sport", "ns:olympics.olympic_participating_country", "0.009346"},
                {"agriculture", "France", "ns:time.event.locations", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:time.event", "ns:location.location", "0.007788"},
                {"agriculture", "France", "ns:time.event.locations", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:time.event", "ns:location.dated_location", "0.007009"},
                {"agriculture", "France", "ns:time.event.locations", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:time.event", "ns:location.statistical_region", "0.007009"},
                {"agriculture", "France", "ns:time.event.locations", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:time.event", "ns:location.country", "0.007009"},
                {"agriculture", "France", "ns:time.event.locations", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:time.event", "ns:organization.organization", "0.007009"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.state_province_region", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.administrative_division", "0.006231"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.state_province_region", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.statistical_region", "0.005607"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.state_province_region", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.dated_location", "0.005607"},
                {"agriculture", "France", "ns:organization.organization.headquarters..location.mailing_address.state_province_region", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:organization.organization", "ns:location.location", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.location", "0.006231"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.dated_location", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.statistical_region", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:location.country", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.deceased_person", "ns:organization.organization", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.location", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.dated_location", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.statistical_region", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.country", "0.005607"},
                {"agriculture", "France", "ns:people.deceased_person.place_of_death", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:organization.organization", "0.005607"},
                {"agriculture", "France", "ns:people.person.places_lived..people.place_lived.location", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.location", "0.006231"},
                {"agriculture", "France", "ns:people.person.places_lived..people.place_lived.location", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.dated_location", "0.005607"},
                {"agriculture", "France", "ns:people.person.places_lived..people.place_lived.location", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.statistical_region", "0.005607"},
                {"agriculture", "France", "ns:people.person.places_lived..people.place_lived.location", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:location.country", "0.005607"},
                {"agriculture", "France", "ns:people.person.places_lived..people.place_lived.location", "study [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]", "ns:people.person", "ns:organization.organization", "0.005607"}

        };


        ConstraintSolver.ConstraintSolverBuilder<String> builder = new ConstraintSolver.ConstraintSolverBuilder<>();

        for (String[] objects : data) {
            builder.add(objects[0], objects[1], objects[3],objects[2], objects[4],objects[5], Doubles.tryParse(objects[6]), 1, 1);
        }

        List<Solution<String>> solutions = builder.build().solve(new PrintCollector(true));

        for (Solution<String> entry : solutions) {
            System.out.println(entry.getLeft() + " -> " + entry.getRight() + "  " + entry.getEdge());
        }


    }

    @Test
    public void testResolveBreak() throws Exception {

        String data[][] = {
                {"Harry", "Maryville High School", "ns:people.person.education..education.education.institution", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization/educational_institution", "0.831425"},
                {"Harry", "Maryville High School", "ns:people.person.education..education.education.institution", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization", "0.831425"},
                {"Harry", "Maryville High School", "ns:location.location.contains", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/location", "/location", "0.023784"},
                {"Harry", "Maryville High School", "ns:people.person.employment_history..business.employment_tenure.company", "[X] graduate from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/business/employer", "0.017617"},
                {"Harry", "University of Tennessee", "ns:education.educational_degree.people_with_this_degree..education.education.institution", "[X] degree from [Y] [1-poss-0,1-prep-2,2-pobj-3]", "/education/educational_degree", "/organization/educational_institution", "0.848325"},
                {"Harry", "University of Tennessee", "ns:education.educational_degree.people_with_this_degree..education.education.institution", "[X] degree from [Y] [1-poss-0,1-prep-2,2-pobj-3]", "/education/educational_degree", "/organization", "0.848325"},
                {"Harry", "Mary", "ns:people.person.spouse_s..people.marriage.spouse", "[X] marry to [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]", "/person", "/person", "0.803797"},
                {"Harry", "law enforcement", "ns:people.person.employment_history..business.employment_tenure.company", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/business/employer", "0.239114"},
                {"Harry", "law enforcement", "ns:military.military_person.service..military.military_service.military_force", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/military/military_person", "/military", "0.152768"},
                {"Harry", "law enforcement", "ns:military.military_person.service..military.military_service.military_force", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/military/military_person", "/organization", "0.152768"},
                {"Harry", "law enforcement", "ns:military.military_person.service..military.military_service.military_force", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/military", "0.152768"},
                {"Harry", "law enforcement", "ns:military.military_person.service..military.military_service.military_force", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization", "0.152768"},
                {"Harry", "law enforcement", "ns:government.politician.government_positions_held..government.government_position_held.governmental_body", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person/politician", "/government/government", "0.063100"},
                {"Harry", "law enforcement", "ns:government.politician.government_positions_held..government.government_position_held.governmental_body", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/government/government", "0.063100"},
                {"Harry", "law enforcement", "ns:people.person.education..education.education.institution", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization/educational_institution", "0.058118"},
                {"Harry", "law enforcement", "ns:people.person.education..education.education.institution", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization", "0.058118"},
                {"Harry", "law enforcement", "ns:organization.organization_founder.organizations_founded", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/organization/organization_founder", "/organization", "0.039852"},
                {"Harry", "law enforcement", "ns:organization.organization_founder.organizations_founded", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization", "0.039852"},
                {"Harry", "law enforcement", "ns:spaceflight.astronaut.space_agency", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/spaceflight/astronaut", "/spaceflight/space_agency", "0.036531"},
                {"Harry", "law enforcement", "ns:spaceflight.astronaut.space_agency", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/spaceflight/astronaut", "/organization", "0.036531"},
                {"Harry", "law enforcement", "ns:spaceflight.astronaut.space_agency", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/spaceflight/space_agency", "0.036531"},
                {"Harry", "law enforcement", "ns:spaceflight.astronaut.space_agency", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization", "0.036531"},
                {"Harry", "law enforcement", "ns:law.judge.courts..law.judicial_tenure.court", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/government_agency", "0.029889"},
                {"Harry", "law enforcement", "ns:business.board_member.organization_board_memberships..organization.organization_board_membership.organization", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/business/board_member", "/organization", "0.029889"},
                {"Harry", "law enforcement", "ns:business.board_member.organization_board_memberships..organization.organization_board_membership.organization", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization", "0.029889"},
                {"Harry", "law enforcement", "ns:sports.pro_athlete.teams..sports.sports_team_roster.team", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person/athlete", "/organization/sports_team", "0.018266"},
                {"Harry", "law enforcement", "ns:sports.pro_athlete.teams..sports.sports_team_roster.team", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person/athlete", "/organization", "0.018266"},
                {"Harry", "law enforcement", "ns:sports.pro_athlete.teams..sports.sports_team_roster.team", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization/sports_team", "0.018266"},
                {"Harry", "law enforcement", "ns:sports.pro_athlete.teams..sports.sports_team_roster.team", "[X] retire from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]", "/person", "/organization", "0.018266"}
        };

        ConstraintSolver.ConstraintSolverBuilder<String> builder = new ConstraintSolver.ConstraintSolverBuilder<>();

        for (String[] objects : data) {
            builder.add(objects[0], objects[1], objects[3],objects[2], objects[4],objects[5], Doubles.tryParse(objects[6]), 1, 1);
        }

        List<Solution<String>> solutions = builder.build().solve(new PrintCollector(true));

        // sort
        Collections.sort(solutions, new Comparator<Solution<String>>() {
            @Override
            public int compare(Solution<String> o1, Solution<String> o2) {
                return ComparisonChain.start()
                        .compare(o1.getLeft(), o2.getLeft())
                        .compare(o1.getRight(), o2.getRight())
                        .compare(o1.getEdge().getRelation(), o2.getEdge().getRelation())
                        .result();
            }
        });

        for (Solution<String> entry : solutions) {
            LOG.info(entry.getLeft() + " -> " + entry.getRight() + "  " + entry.getEdge());
        }

        Assert.assertThat(solutions.size(), is(3));
        Assert.assertThat(solutions.get(0).getLeft(), is("Harry"));
        Assert.assertThat(solutions.get(0).getRight(), is("Mary"));
        Assert.assertThat(solutions.get(0).getEdge().getRelation(), is("ns:people.person.spouse_s..people.marriage.spouse"));

    }


}
