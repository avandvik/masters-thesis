package utils;

import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class HelpersTest {

    @Test
    @DisplayName("test sortOrdersFromInst")
    public void sortOrdersFromInstTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        for (int instId = 1; instId < 20; instId++) {
            List<Order> ordersFromInst = createOrdersFromInstMDODOP(instId);
            Helpers.sortOrdersFromInst(ordersFromInst);
            assertTrue(ordersFromInst.get(0).isMandatory());
            assertTrue(!ordersFromInst.get(1).isMandatory() && ordersFromInst.get(1).isDelivery());
            assertTrue(!ordersFromInst.get(2).isMandatory() && !ordersFromInst.get(2).isDelivery());
        }
        for (int instId = 1; instId < 15; instId++) {
            List<Order> ordersFromInst = createOrdersFromInstMDOD(instId);
            Helpers.sortOrdersFromInst(ordersFromInst);
            assertTrue(ordersFromInst.get(0).isMandatory());
            assertTrue(!ordersFromInst.get(1).isMandatory() && ordersFromInst.get(1).isDelivery());
        }
        for (int instId = 1; instId < 15; instId++) {
            List<Order> ordersFromInst = createOrdersFromInstMDOP(instId);
            Helpers.sortOrdersFromInst(ordersFromInst);
            assertTrue(ordersFromInst.get(0).isMandatory());
            assertTrue(!ordersFromInst.get(1).isMandatory() && !ordersFromInst.get(1).isDelivery());
        }
        for (int instId = 1; instId < 15; instId++) {
            List<Order> ordersFromInst = createOrdersFromInstODOP(instId);
            Helpers.sortOrdersFromInst(ordersFromInst);
            assertTrue(!ordersFromInst.get(0).isMandatory() && ordersFromInst.get(0).isDelivery());
            assertTrue(!ordersFromInst.get(1).isMandatory() && !ordersFromInst.get(1).isDelivery());
        }
        for (int instId = 1; instId < 2; instId++) {
            List<Order> ordersFromInst = createOrdersFromInstMD(instId);
            Helpers.sortOrdersFromInst(ordersFromInst);
            assertTrue(ordersFromInst.get(0).isMandatory());
        }
        for (int instId = 1; instId < 2; instId++) {
            List<Order> ordersFromInst = createOrdersFromInstOD(instId);
            Helpers.sortOrdersFromInst(ordersFromInst);
            assertTrue(!ordersFromInst.get(0).isMandatory() && ordersFromInst.get(0).isDelivery());
        }
        for (int instId = 1; instId < 2; instId++) {
            List<Order> ordersFromInst = createOrdersFromInstOP(instId);
            Helpers.sortOrdersFromInst(ordersFromInst);
            assertTrue(!ordersFromInst.get(0).isMandatory() && !ordersFromInst.get(0).isDelivery());
        }
    }

    private List<Order> createOrdersFromInstMDODOP(int instId) {
        List<Order> ordersFromInst = new ArrayList<>();
        ordersFromInst.add(new Order(0, true, true, 100, instId));
        ordersFromInst.add(new Order(1, false, true, 100, instId));
        ordersFromInst.add(new Order(2, false, false, 100, instId));
        Collections.shuffle(ordersFromInst, Problem.random);
        return ordersFromInst;
    }

    private List<Order> createOrdersFromInstMDOD(int instId) {
        List<Order> ordersFromInst = new ArrayList<>();
        ordersFromInst.add(new Order(0, true, true, 100, instId));
        ordersFromInst.add(new Order(1, false, true, 100, instId));
        Collections.shuffle(ordersFromInst, Problem.random);
        return ordersFromInst;
    }

    private List<Order> createOrdersFromInstMDOP(int instId) {
        List<Order> ordersFromInst = new ArrayList<>();
        ordersFromInst.add(new Order(0, true, true, 100, instId));
        ordersFromInst.add(new Order(1, false, false, 100, instId));
        Collections.shuffle(ordersFromInst, Problem.random);
        return ordersFromInst;
    }

    private List<Order> createOrdersFromInstODOP(int instId) {
        List<Order> ordersFromInst = new ArrayList<>();
        ordersFromInst.add(new Order(0, false, true, 100, instId));
        ordersFromInst.add(new Order(1, false, false, 100, instId));
        Collections.shuffle(ordersFromInst, Problem.random);
        return ordersFromInst;
    }

    private List<Order> createOrdersFromInstMD(int instId) {
        List<Order> ordersFromInst = new ArrayList<>();
        ordersFromInst.add(new Order(0, true, true, 100, instId));
        Collections.shuffle(ordersFromInst, Problem.random);
        return ordersFromInst;
    }

    private List<Order> createOrdersFromInstOD(int instId) {
        List<Order> ordersFromInst = new ArrayList<>();
        ordersFromInst.add(new Order(0, false, true, 100, instId));
        Collections.shuffle(ordersFromInst, Problem.random);
        return ordersFromInst;
    }

    private List<Order> createOrdersFromInstOP(int instId) {
        List<Order> ordersFromInst = new ArrayList<>();
        ordersFromInst.add(new Order(0, false, false, 100, instId));
        Collections.shuffle(ordersFromInst, Problem.random);
        return ordersFromInst;
    }
}
