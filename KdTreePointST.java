import dsa.LinkedQueue;
import dsa.MaxPQ;
import dsa.Point2D;
import dsa.RectHV;
import stdlib.StdIn;
import stdlib.StdOut;

public class KdTreePointST<Value> implements PointST<Value> {
    private Node root;
    private int size;

    // Constructs an empty symbol table.
    public KdTreePointST() {
        root = null;
        size = 0;
    }

    // Returns true if this symbol table is empty, and false otherwise.
    public boolean isEmpty() {
        return size() == 0;
    }

    // Returns the number of key-value pairs in this symbol table.
    public int size() {
        return size;
    }

    // Inserts the given point and value into this symbol table.
    public void put(Point2D p, Value value) {
        if (p == null) throw new NullPointerException("p is null");
        if (value == null) throw new NullPointerException("value is null");
        root = put(root, p, value, new RectHV(0, 0, 1, 1), true);
    }

    // Returns the value associated with the given point in this symbol table, or null.
    public Value get(Point2D p) {
        if (p == null) throw new NullPointerException("p is null");
        return get(root, p, true);
    }

    // Returns true if this symbol table contains the given point, and false otherwise.
    public boolean contains(Point2D p) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        return get(p) != null;
    }

    // Returns all the points in this symbol table.
    public Iterable<Point2D> points() {
        Queue<Node> qIn = new Queue<Node>();
        Queue<Point2D> qOut = new Queue<Point2D>();
        qIn.enqueue(root);
        while (!qIn.isEmpty()){
            Node x = qIn.dequeue();
            if (x.lb != null) {
                qIn.enqueue(x.lb);
            }
            if (x.rt != null) {
                qIn.enqueue(x.rt);
            }
            qOut.enqueue(x.p);
        }
        return qOut;
    }

    // Returns all the points in this symbol table that are inside the given rectangle.
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) {
            throw new NullPointerException("rect is null");
        }
        Queue<Point2D> queue = new LinkedList<Point2D>();
        range(root, rect, queue);
        return queue;
    }

    // Returns the point in this symbol table that is different from and closest to the given point,
    // or null.
    public Point2D nearest(Point2D p) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        if (isEmpty()) {
            return null;
        }
        return nearest(root, p, root.p, true);
    }

    // Returns up to k points from this symbol table that are different from and closest to the
    // given point.
    public Iterable<Point2D> nearest(Point2D p, int k) {
        MaxPQ<Point2D> pq = new MaxPQ<Point2D>(p.distanceToOrder());
        nearest(root, p, k, pq, true);
        return pq;
    }

    // Note: In the helper methods that have lr as a parameter, its value specifies how to
    // compare the point p with the point x.p. If true, the points are compared by their
    // x-coordinates; otherwise, the points are compared by their y-coordinates. If the
    // comparison of the coordinates (x or y) is true, the recursive call is made on x.lb;
    // otherwise, the call is made on x.rt.

    // Inserts the given point and value into the KdTree x having rect as its axis-aligned
    // rectangle, and returns a reference to the modified tree.
    private Node put(Node x, Point2D p, Value value, RectHV rect, boolean lr) {
        if (x == null) {
            size++;
            return new Node(p, value, rect);
        }
        if (x.p.equals(p)){
            x.value = value;
        } else if (lr && p.x() < x.p.x() || !lr &&  p.y() < x.p.y()){
            RectHV subRect = (lr)
                    ? new RectHV(rect.xmin(), rect.ymin(), x.p.x(), rect.ymax())
                    : new RectHV(rect.xmin(), rect.ymin(), rect.xmax(), x.p.y());
            x.lb = put(x.lb, p, value, subRect, !lr);
        } else {
            RectHV subRect = (lr)
                    ? new RectHV(x.p.x(), rect.ymin(), rect.xmax(), rect.ymax())
                    : new RectHV(rect.xmin(), x.p.y(), rect.xmax(), rect.ymax());
            x.rt = put(x.rt, p, value, subRect, !lr);
        }
        return x;
    }

    // Returns the value associated with the given point in the KdTree x, or null.
    private Value get(Node x, Point2D p, boolean lr) {
        if (x == null){
            return null;
        }
        if (x.p.equals(p)){
            return x.value;
        }
        else if (lr && p.x() < x.p.x() || !lr && p.y() < x.p.y()){
            return get(x.lb, p, !lr);
        }
        return get(x.rt, p, !lr);
    }

    // Collects in the given queue all the points in the KdTree x that are inside rect.
    private void range(Node x, RectHV rect, LinkedQueue<Point2D> q) {
        if (node == null) {
            return;
        }
        if (rect.contains(node.point)) {
            q.add(node.point);
        }
        if (node.left != null && rect.intersects(node.left.rect)) {
            range(node.left, rect, q);
        }
        if (node.right != null && rect.intersects(node.right.rect)) {
            range(node.right, rect, q);
        }
    }

    // Returns the point in the KdTree x that is closest to p, or null; nearest is the closest
    // point discovered so far.
    private Point2D nearest(Node x, Point2D p, Point2D nearest, boolean lr) {
        if (node == null) {
            return nearest;
        }
        if (node.point.distanceSquaredTo(p) < nearest.distanceSquaredTo(p)) {
            nearest = node.point;
        }
        double cmp = lr ? p.x() - node.point.x() : p.y() - node.point.y();
        Node first = cmp < 0 ? node.left : node.right;
        Node second = cmp < 0 ? node.right : node.left;
        nearest = nearest(first, p, nearest, !lr);
        if (second != null && second.rect.distanceSquaredTo(p) < nearest.distanceSquaredTo(p)) {
            nearest = nearest(second, p, nearest, !lr);
        }
        return nearest;
    }

    // Collects in the given max-PQ up to k points from the KdTree x that are different from and
    // closest to p.
    private void nearest(Node x, Point2D p, int k, MaxPQ<Point2D> pq, boolean lr) {
        if (x == null) {
            return;
        }
        double distToP = x.point.distanceSquaredTo(p);
        if (pq.size() < k || distToP < pq.max().distanceSquaredTo(p)) {
            pq.insert(x.point);
            if (pq.size() > k) {
                pq.delMax();
            }
        }
        Node firstNode, secondNode;
        double cmp;
        if (lr) {
            firstNode = x.right;
            secondNode = x.left;
            cmp = p.x() - x.point.x();
        } else {
            firstNode = x.left;
            secondNode = x.right;
            cmp = p.y() - x.point.y();
        }
        nearest(firstNode, p, k, pq, !lr);
        if (pq.size() < k || cmp * cmp < pq.max().distanceSquaredTo(p)) {
            nearest(secondNode, p, k, pq, !lr);
        }
    }

    // A representation of node in a KdTree in two dimensions (ie, a 2dTree). Each node stores a
    // 2d point (the key), a value, an axis-aligned rectangle, and references to the left/bottom
    // and right/top subtrees.
    private class Node {
        private Point2D p;   // the point (key)
        private Value value; // the value
        private RectHV rect; // the axis-aligned rectangle
        private Node lb;     // the left/bottom subtree
        private Node rt;     // the right/top subtree

        // Constructs a node given the point (key), the associated value, and the
        // corresponding axis-aligned rectangle.
        Node(Point2D p, Value value, RectHV rect) {
            this.p = p;
            this.value = value;
            this.rect = rect;
        }
    }

    // Unit tests the data type. [DO NOT EDIT]
    public static void main(String[] args) {
        KdTreePointST<Integer> st = new KdTreePointST<>();
        double qx = Double.parseDouble(args[0]);
        double qy = Double.parseDouble(args[1]);
        int k = Integer.parseInt(args[2]);
        Point2D query = new Point2D(qx, qy);
        RectHV rect = new RectHV(-1, -1, 1, 1);
        int i = 0;
        while (!StdIn.isEmpty()) {
            double x = StdIn.readDouble();
            double y = StdIn.readDouble();
            Point2D p = new Point2D(x, y);
            st.put(p, i++);
        }
        StdOut.println("st.empty()? " + st.isEmpty());
        StdOut.println("st.size() = " + st.size());
        StdOut.printf("st.contains(%s)? %s\n", query, st.contains(query));
        StdOut.printf("st.range(%s):\n", rect);
        for (Point2D p : st.range(rect)) {
            StdOut.println("  " + p);
        }
        StdOut.printf("st.nearest(%s) = %s\n", query, st.nearest(query));
        StdOut.printf("st.nearest(%s, %d):\n", query, k);
        for (Point2D p : st.nearest(query, k)) {
            StdOut.println("  " + p);
        }
    }
}
