package com.kmarlow.custominstrumentation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static com.kmarlow.custominstrumentation.Preconditions.checkArgument;
import static java.util.Collections.unmodifiableList;

public class ADMStack implements Iterable<String> {
    private final List<String> stack;

    @NonNull
    public static Builder emptyBuilder() {
        return new Builder(Collections.<String>emptyList());
    }

    /**
     * Create a stack that contains a single key.
     */
    @NonNull
    public static ADMStack single(@NonNull String key) {
        return emptyBuilder().push(key).build();
    }

    private ADMStack(List<String> stack) {
        checkArgument(stack != null && !stack.isEmpty(), "Stack may not be empty");
        this.stack = stack;
    }

    @NonNull
    public <T> Iterator<T> reverseIterator() {
        return new ReadStateIterator<>(stack.iterator());
    }

    @NonNull
    @Override
    public Iterator<String> iterator() {
        return new ReadStateIterator<>(new ReverseIterator<>(stack));
    }

    public int size() {
        return stack.size();
    }

    @NonNull
    public <T> T top() {
        return peek(0);
    }

    /**
     * Returns the app state at the provided index in stack. 0 is the newest entry.
     */
    @NonNull
    public <T> T peek(int index) {
        //noinspection unchecked
        return (T) stack.get(stack.size() - index - 1);
    }

    @NonNull
    List<String> asList() {
        final ArrayList<String> copy = new ArrayList<>(stack);
        return unmodifiableList(copy);
    }

    /**
     * Get a builder to modify a copy of this stack.
     * <p>
     * The builder returned will retain all internal information related to the keys in the
     * stack, including their states. It is safe to remove keys from the builder and push them back
     * on; nothing will be lost in those operations.
     */
    @NonNull
    public Builder buildUpon() {
        return new Builder(stack);
    }

    @Override
    public String toString() {
        return Arrays.deepToString(stack.toArray());
    }

    public static final class Builder {
        private final List<String> stack;

        private Builder(Collection<String> stack) {
            this.stack = new ArrayList<>(stack);
        }

        /**
         * Removes all keys from this builder. But note that if this builder was created
         * via {@link #buildUpon()}, any state associated with the cleared
         * keys will be preserved and will be restored if they are {@link #push pushed}
         * back on.
         */
        @NonNull
        public Builder clear() {
            // Clear by popping everything (rather than just calling stack.clear()) to
            // fill up entryMemory. Otherwise we drop view state on the floor.
            while (!isEmpty()) {
                pop();
            }

            return this;
        }

        /**
         * Adds a key to the builder. If this builder was created via {@link #buildUpon()},
         * and the pushed key was previously {@link #pop() popped} or {@link #clear cleared}
         * from the builder, the key's associated state will be restored.
         */
        @NonNull
        public Builder push(@NonNull String key) {
            stack.add(key);
            return this;
        }

        /**
         * {@link #push Pushes} all of the keys in the collection onto this builder.
         */
        @NonNull
        public Builder pushAll(@NonNull Collection<String> c) {
            for (String key : c) {
                //noinspection CheckResult
                push(key);
            }
            return this;
        }

        /**
         * @return null if the stack is empty.
         */
        @Nullable
        public Object peek() {
            return stack.isEmpty() ? null : stack.get(stack.size() - 1);
        }

        @NonNull
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        /**
         * Removes the last state added. Note that if this builder was created
         * via {@link #buildUpon()}, any view state associated with the popped
         * state will be preserved, and restored if it is {@link #push pushed}
         * back in.
         *
         * @throws IllegalStateException if empty
         */
        public String pop() {
            if (isEmpty()) {
                throw new IllegalStateException("Cannot pop from an empty builder");
            }
            return stack.remove(stack.size() - 1);
        }

        /**
         * Pops the stack until the given state is at the top.
         *
         * @throws IllegalArgumentException if the given state isn't in the stack.
         */
        @NonNull
        public Builder popTo(@NonNull String state) {
            //noinspection ConstantConditions
            while (!isEmpty() && !peek().equals(state)) {
                pop();
            }
            checkArgument(!isEmpty(), String.format("%s not found in stack", state));
            return this;
        }

        @NonNull
        public Builder pop(int count) {
            final int size = stack.size();
            checkArgument(count <= size,
                    String.format((Locale) null, "Cannot pop %d elements, stack only has %d", count, size));
            while (count-- > 0) {
                pop();
            }
            return this;
        }

        @NonNull
        public ADMStack build() {
            return new ADMStack(stack);
        }

        @Override
        public String toString() {
            return Arrays.deepToString(stack.toArray());
        }
    }

    private static class ReverseIterator<T> implements Iterator<T> {
        private final ListIterator<T> wrapped;

        ReverseIterator(List<T> list) {
            wrapped = list.listIterator(list.size());
        }

        @Override
        public boolean hasNext() {
            return wrapped.hasPrevious();
        }

        @Override
        public T next() {
            return wrapped.previous();
        }

        @Override
        public void remove() {
            wrapped.remove();
        }
    }

    private static class ReadStateIterator<T> implements Iterator<T> {
        private final Iterator<String> iterator;

        ReadStateIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            //noinspection unchecked
            return (T) iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
