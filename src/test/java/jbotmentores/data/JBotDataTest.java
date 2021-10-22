package jbotmentores.data;

import jbotmentores.model.JBotData;
import jbotmentores.model.Mentor;
import jbotmentores.model.Skill;
import jbotmentores.model.Slot;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Set;

public class JBotDataTest {

    @Test
    public void testFindMentoresBySkill() {
        Set<Slot> slots = Set.of(new Slot(LocalDateTime.now(), LocalDateTime.now()));

        Mentor mentor1 = new Mentor("gabriela@a.b", "Gabriela", Set.of(new Skill("JAVA"), new Skill("Testes"), new Skill("Ideacao")), slots);
        Mentor mentor2 = new Mentor("max@a.b", "Max", Set.of(new Skill("Kubernetes"), new Skill("Desenvolvimento"), new Skill("java")), slots);
        Mentor mentor3 = new Mentor("teste@a.b", "Teste", Set.of(new Skill("Testes"), new Skill("Desenvolvimento java"), new Skill("Ideacao")), slots);

        Set<Mentor> mentores = Set.of(mentor1, mentor2, mentor3);

        JBotData jbotData = new JBotData();
        jbotData.mentores = mentores;

        Assert.assertTrue(jbotData.listMentoresBySkill("Java").size() == 3);
    }


}
