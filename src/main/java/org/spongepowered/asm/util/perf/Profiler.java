package org.spongepowered.asm.util.perf;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.include.com.google.common.base.Joiner;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

public final class Profiler {
   public static final int ROOT = 1;
   public static final int FINE = 2;
   private static final Map<String, Profiler> profilers = new HashMap<>();
   private static boolean active;
   private final String id;
   private final Map<String, Profiler.Section> sections = new TreeMap<>();
   private final List<String> phases = new ArrayList<>();
   private final Deque<Profiler.Section> stack = new LinkedList<>();

   public Profiler(String id) {
      this.id = id;
      this.phases.add("Initial");
   }

   @Override
   public String toString() {
      return this.id;
   }

   public static void setActive(boolean active) {
      Profiler.active = active;
   }

   public synchronized void reset() {
      for (Profiler.Section section : this.sections.values()) {
         section.invalidate();
      }

      this.sections.clear();
      this.phases.clear();
      this.phases.add("Initial");
      this.stack.clear();
   }

   public synchronized Profiler.Section get(String name) {
      Profiler.Section section = this.sections.get(name);
      if (section == null) {
         section = (Profiler.Section)(active ? new Profiler.LiveSection(name, this.phases.size() - 1) : new Profiler.DisabledSection(name));
         this.sections.put(name, section);
      }

      return section;
   }

   private synchronized Profiler.Section getSubSection(String name, String baseName, Profiler.Section root) {
      Profiler.Section section = this.sections.get(name);
      if (section == null) {
         section = new Profiler.SubSection(name, this.phases.size() - 1, baseName, root);
         this.sections.put(name, section);
      }

      return section;
   }

   public Profiler.Section begin(String... path) {
      return this.begin(0, path);
   }

   public Profiler.Section begin(int flags, String... path) {
      return this.begin(flags, Joiner.on('.').join(path));
   }

   public Profiler.Section begin(String name) {
      return this.begin(0, name);
   }

   public synchronized Profiler.Section begin(int flags, String name) {
      boolean root = (flags & 1) != 0;
      boolean fine = (flags & 2) != 0;
      String path = name;
      Profiler.Section head = this.stack.peek();
      if (head != null) {
         path = head.getName() + (root ? " -> " : ".") + name;
         if (head.isRoot() && !root) {
            int pos = head.getName().lastIndexOf(" -> ");
            name = (pos > -1 ? head.getName().substring(pos + 4) : head.getName()) + "." + name;
            root = true;
         }
      }

      Profiler.Section section = this.get(root ? name : path);
      if (root && head != null && active) {
         section = this.getSubSection(path, head.getName(), section);
      }

      section.setFine(fine).setRoot(root);
      this.stack.push(section);
      return section.start();
   }

   synchronized void end(Profiler.Section section) {
      try {
         Profiler.Section head = this.stack.pop();

         for (Profiler.Section next = head; next != section; next = this.stack.pop()) {
            if (next == null && active) {
               if (head == null) {
                  throw new IllegalStateException("Attempted to pop " + section + " but the stack is empty");
               }

               throw new IllegalStateException("Attempted to pop " + section + " which was not in the stack, head was " + head);
            }
         }
      } catch (NoSuchElementException var4) {
         if (active) {
            throw new IllegalStateException("Attempted to pop " + section + " but the stack is empty");
         }
      }
   }

   public synchronized void mark(String phase) {
      long currentPhaseTime = 0L;

      for (Profiler.Section section : this.sections.values()) {
         currentPhaseTime += section.getTime();
      }

      if (currentPhaseTime == 0L) {
         int size = this.phases.size();
         this.phases.set(size - 1, phase);
      } else {
         this.phases.add(phase);

         for (Profiler.Section section : this.sections.values()) {
            section.mark();
         }
      }
   }

   public synchronized Collection<Profiler.Section> getSections() {
      return Collections.unmodifiableCollection(this.sections.values());
   }

   public PrettyPrinter printer(boolean includeFine, boolean group) {
      return printer(includeFine, group, this.phases, this.sections);
   }

   private static PrettyPrinter printer(boolean includeFine, boolean group, List<String> phases, Map<String, Profiler.Section> sections) {
      PrettyPrinter printer = new PrettyPrinter();
      int colCount = phases.size() + 4;
      int[] columns = new int[]{0, 1, 2, colCount - 2, colCount - 1};
      Object[] headers = new Object[colCount * 2];
      int col = 0;

      for (int pos = 0; col < colCount; pos = ++col * 2) {
         headers[pos + 1] = PrettyPrinter.Alignment.RIGHT;
         if (col == columns[0]) {
            headers[pos] = (group ? "" : "  ") + "Section";
            headers[pos + 1] = PrettyPrinter.Alignment.LEFT;
         } else if (col == columns[1]) {
            headers[pos] = "    TOTAL";
         } else if (col == columns[3]) {
            headers[pos] = "    Count";
         } else if (col == columns[4]) {
            headers[pos] = "Avg. ";
         } else if (col - columns[2] < phases.size()) {
            headers[pos] = phases.get(col - columns[2]);
         } else {
            headers[pos] = "";
         }
      }

      printer.table(headers).th().hr().add();

      for (Profiler.Section section : sections.values()) {
         if ((!section.isFine() || includeFine) && (!group || section.getDelegate() == section)) {
            printSectionRow(printer, colCount, columns, section, group);
            if (group) {
               for (Profiler.Section subSection : sections.values()) {
                  Profiler.Section delegate = subSection.getDelegate();
                  if ((!subSection.isFine() || includeFine) && delegate == section && delegate != subSection) {
                     printSectionRow(printer, colCount, columns, subSection, group);
                  }
               }
            }
         }
      }

      return printer.add();
   }

   private static void printSectionRow(PrettyPrinter printer, int colCount, int[] columns, Profiler.Section section, boolean group) {
      boolean isDelegate = section.getDelegate() != section;
      Object[] values = new Object[colCount];
      int col = 1;
      if (group) {
         values[0] = isDelegate ? "  > " + section.getBaseName() : section.getName();
      } else {
         values[0] = (isDelegate ? "+ " : "  ") + section.getName();
      }

      long[] times = section.getTimes();

      for (long time : times) {
         if (col == columns[1]) {
            values[col++] = section.getTotalTime() + " ms";
         }

         if (col >= columns[2] && col < values.length) {
            values[col++] = time + " ms";
         }
      }

      values[columns[3]] = section.getTotalCount();
      values[columns[4]] = new DecimalFormat("   ###0.000 ms").format(section.getTotalAverageTime());

      for (int i = 0; i < values.length; i++) {
         if (values[i] == null) {
            values[i] = "-";
         }
      }

      printer.tr(values);
   }

   public void printSummary() {
      printSummary(this.id, this.phases, this.sections);
   }

   public static void printAuditSummary() {
      String id;
      Set<String> allPhases;
      Map<String, Profiler.Section> allSections;
      synchronized (profilers) {
         id = Joiner.on(',').join(profilers.values());
         allPhases = new LinkedHashSet<>();
         allSections = new TreeMap<String, Profiler.Section>() {
            public Profiler.Section get(Object name) {
               Profiler.Section section = (Profiler.Section)super.get(name);
               if (section == null) {
                  this.put(name.toString(), section = new Profiler.ResultSection(name.toString()));
               }

               return section;
            }
         };

         for (Profiler profiler : profilers.values()) {
            for (String phase : profiler.phases) {
               allPhases.add(phase);
            }

            for (Entry<String, Profiler.Section> section : profiler.sections.entrySet()) {
               ((Profiler.ResultSection)allSections.get(section.getKey())).add(section.getValue());
            }
         }
      }

      printSummary(id, new ArrayList<>(allPhases), allSections);
   }

   private static void printSummary(String id, List<String> phases, Map<String, Profiler.Section> sections) {
      DecimalFormat threedp = new DecimalFormat("(###0.000");
      DecimalFormat onedp = new DecimalFormat("(###0.0");
      PrettyPrinter printer = printer(false, false, phases, sections);
      long prepareTime = sections.get("mixin.prepare").getTotalTime();
      long readTime = sections.get("mixin.read").getTotalTime();
      long applyTime = sections.get("mixin.apply").getTotalTime();
      long writeTime = sections.get("mixin.write").getTotalTime();
      long totalMixinTime = sections.get("mixin").getTotalTime();
      long loadTime = sections.get("class.load").getTotalTime();
      long transformTime = sections.get("class.transform").getTotalTime();
      long exportTime = sections.get("mixin.debug.export").getTotalTime();
      long actualTime = totalMixinTime - loadTime - transformTime - exportTime;
      double timeSliceMixin = (double)actualTime / totalMixinTime * 100.0;
      double timeSliceLoad = (double)loadTime / totalMixinTime * 100.0;
      double timeSliceTransform = (double)transformTime / totalMixinTime * 100.0;
      double timeSliceExport = (double)exportTime / totalMixinTime * 100.0;
      long worstTransformerTime = 0L;
      Profiler.Section worstTransformer = null;

      for (Profiler.Section section : sections.values()) {
         long transformerTime = section.getName().startsWith("class.transform.") ? section.getTotalTime() : 0L;
         if (transformerTime > worstTransformerTime) {
            worstTransformerTime = transformerTime;
            worstTransformer = section;
         }
      }

      printer.hr().add("Summary for Profiler[%s]", id).hr().add();
      String format = "%9d ms %12s seconds)";
      printer.kv("Total mixin time", format, totalMixinTime, threedp.format(totalMixinTime * 0.001)).add();
      printer.kv("Preparing mixins", format, prepareTime, threedp.format(prepareTime * 0.001));
      printer.kv("Reading input", format, readTime, threedp.format(readTime * 0.001));
      printer.kv("Applying mixins", format, applyTime, threedp.format(applyTime * 0.001));
      printer.kv("Writing output", format, writeTime, threedp.format(writeTime * 0.001)).add();
      printer.kv("of which", "");
      printer.kv("Time spent loading from disk", format, loadTime, threedp.format(loadTime * 0.001));
      printer.kv("Time spent transforming classes", format, transformTime, threedp.format(transformTime * 0.001)).add();
      if (worstTransformer != null) {
         printer.kv("Worst transformer", worstTransformer.getName());
         printer.kv("Class", worstTransformer.getInfo());
         printer.kv("Time spent", "%s seconds", worstTransformer.getTotalSeconds());
         printer.kv("called", "%d times", worstTransformer.getTotalCount()).add();
      }

      printer.kv("   Time allocation:     Processing mixins", "%9d ms %10s%% of total)", actualTime, onedp.format(timeSliceMixin));
      printer.kv("Loading classes", "%9d ms %10s%% of total)", loadTime, onedp.format(timeSliceLoad));
      printer.kv("Running transformers", "%9d ms %10s%% of total)", transformTime, onedp.format(timeSliceTransform));
      if (exportTime > 0L) {
         printer.kv("Exporting classes (debug)", "%9d ms %10s%% of total)", exportTime, onedp.format(timeSliceExport));
      }

      printer.add();

      try {
         Class<?> agent = MixinService.getService().getClassProvider().findAgentClass("org.spongepowered.metronome.Agent", false);
         Method mdGetTimes = agent.getDeclaredMethod("getTimes");
         Map<String, Long> times = (Map<String, Long>)mdGetTimes.invoke(null);
         printer.hr().add("Transformer Times").hr().add();
         int longest = 10;

         for (Entry<String, Long> entry : times.entrySet()) {
            longest = Math.max(longest, entry.getKey().length());
         }

         for (Entry<String, Long> entry : times.entrySet()) {
            String name = entry.getKey();
            long mixinTime = 0L;

            for (Profiler.Section sectionx : sections.values()) {
               if (name.equals(sectionx.getInfo())) {
                  mixinTime = sectionx.getTotalTime();
                  break;
               }
            }

            if (mixinTime > 0L) {
               printer.add("%-" + longest + "s %8s ms %8s ms in mixin)", name, entry.getValue() + mixinTime, "(" + mixinTime);
            } else {
               printer.add("%-" + longest + "s %8s ms", name, entry.getValue());
            }
         }

         printer.add();
      } catch (Throwable var47) {
      }

      printer.print();
   }

   public static Profiler getProfiler(String id) {
      synchronized (profilers) {
         Profiler profiler = profilers.get(id);
         if (profiler == null) {
            profilers.put(id, profiler = new Profiler(id));
         }

         return profiler;
      }
   }

   public static Collection<Profiler> getProfilers() {
      ImmutableList.Builder<Profiler> list = ImmutableList.builder();
      synchronized (profilers) {
         list.addAll(profilers.values());
      }

      return list.build();
   }

   class DisabledSection extends Profiler.Section {
      DisabledSection(String name) {
         super(name);
      }

      @Override
      public Profiler.Section end() {
         if (!this.invalidated) {
            Profiler.this.end(this);
         }

         return this;
      }

      @Override
      public Profiler.Section next(String name) {
         this.end();
         return Profiler.this.begin(name);
      }
   }

   class LiveSection extends Profiler.DisabledSection {
      private int cursor = 0;
      private long[] times = new long[0];
      private long start = 0L;
      private long time;
      private long markedTime;
      private int count;
      private int markedCount;

      LiveSection(String name, int cursor) {
         super(name);
         this.cursor = cursor;
      }

      @Override
      protected int getCursor() {
         return this.cursor;
      }

      @Override
      Profiler.Section start() {
         this.start = System.currentTimeMillis();
         return this;
      }

      @Override
      protected Profiler.Section stop() {
         if (this.start > 0L) {
            this.time = this.time + (System.currentTimeMillis() - this.start);
         }

         this.start = 0L;
         this.count++;
         return this;
      }

      @Override
      public Profiler.Section end() {
         this.stop();
         if (!this.invalidated) {
            Profiler.this.end(this);
         }

         return this;
      }

      @Override
      void mark() {
         if (this.cursor >= this.times.length) {
            this.times = Arrays.copyOf(this.times, this.cursor + 4);
         }

         this.times[this.cursor] = this.time;
         this.markedTime = this.markedTime + this.time;
         this.markedCount = this.markedCount + this.count;
         this.time = 0L;
         this.count = 0;
         this.cursor++;
      }

      @Override
      public long getTime() {
         return this.time;
      }

      @Override
      public long getTotalTime() {
         return this.time + this.markedTime;
      }

      @Override
      public double getSeconds() {
         return this.time * 0.001;
      }

      @Override
      public double getTotalSeconds() {
         return (this.time + this.markedTime) * 0.001;
      }

      @Override
      public long[] getTimes() {
         long[] times = new long[this.cursor + 1];
         System.arraycopy(this.times, 0, times, 0, Math.min(this.times.length, this.cursor));
         times[this.cursor] = this.time;
         return times;
      }

      @Override
      public int getCount() {
         return this.count;
      }

      @Override
      public int getTotalCount() {
         return this.count + this.markedCount;
      }

      @Override
      public double getAverageTime() {
         return this.count > 0 ? (double)this.time / this.count : 0.0;
      }

      @Override
      public double getTotalAverageTime() {
         return this.count > 0 ? (double)(this.time + this.markedTime) / (this.count + this.markedCount) : 0.0;
      }

      @Override
      protected long getMarkedTime() {
         return this.markedTime;
      }

      @Override
      protected int getMarkedCount() {
         return this.markedCount;
      }
   }

   static class ResultSection extends Profiler.Section {
      private List<Profiler.Section> sections = new ArrayList<>();

      ResultSection(String name) {
         super(name);
      }

      void add(Profiler.Section section) {
         this.sections.add(section);
      }

      @Override
      public long getTime() {
         long time = 0L;

         for (Profiler.Section section : this.sections) {
            time += section.getTime();
         }

         return time;
      }

      @Override
      public long getTotalTime() {
         long totalTime = 0L;

         for (Profiler.Section section : this.sections) {
            totalTime += section.getTotalTime();
         }

         return totalTime;
      }

      @Override
      public double getSeconds() {
         double seconds = 0.0;

         for (Profiler.Section section : this.sections) {
            seconds += section.getSeconds();
         }

         return seconds;
      }

      @Override
      public double getTotalSeconds() {
         double totalSeconds = 0.0;

         for (Profiler.Section section : this.sections) {
            totalSeconds += section.getTotalSeconds();
         }

         return totalSeconds;
      }

      @Override
      public long[] getTimes() {
         int cursor = 0;

         for (Profiler.Section section : this.sections) {
            cursor = Math.max(cursor, section.getCursor());
         }

         long[] times = new long[cursor + 1];

         for (Profiler.Section section : this.sections) {
            long[] sectionTimes = section.getTimes();

            for (int i = 0; i < sectionTimes.length; i++) {
               times[i] += sectionTimes[i];
            }
         }

         return times;
      }

      @Override
      public int getCount() {
         int count = 0;

         for (Profiler.Section section : this.sections) {
            count += section.getCount();
         }

         return count;
      }

      @Override
      public int getTotalCount() {
         int totalCount = 0;

         for (Profiler.Section section : this.sections) {
            totalCount += section.getTotalCount();
         }

         return totalCount;
      }

      @Override
      protected long getMarkedTime() {
         long markedTime = 0L;

         for (Profiler.Section section : this.sections) {
            markedTime += section.getMarkedTime();
         }

         return markedTime;
      }

      @Override
      protected int getMarkedCount() {
         int markedCount = 0;

         for (Profiler.Section section : this.sections) {
            markedCount += section.getMarkedCount();
         }

         return markedCount;
      }

      @Override
      public double getAverageTime() {
         int count = this.getCount();
         return count > 0 ? (double)this.getTime() / count : 0.0;
      }

      @Override
      public double getTotalAverageTime() {
         int count = this.getCount();
         return count > 0 ? (double)(this.getTime() + this.getMarkedTime()) / (count + this.getMarkedCount()) : 0.0;
      }
   }

   public abstract static class Section {
      private final String name;
      private boolean root;
      private boolean fine;
      protected boolean invalidated;
      private String info;

      Section(String name) {
         this.name = name;
         this.info = name;
      }

      protected int getCursor() {
         return 0;
      }

      Profiler.Section getDelegate() {
         return this;
      }

      Profiler.Section invalidate() {
         this.invalidated = true;
         return this;
      }

      Profiler.Section setRoot(boolean root) {
         this.root = root;
         return this;
      }

      public boolean isRoot() {
         return this.root;
      }

      Profiler.Section setFine(boolean fine) {
         this.fine = fine;
         return this;
      }

      public boolean isFine() {
         return this.fine;
      }

      public String getName() {
         return this.name;
      }

      public String getBaseName() {
         return this.name;
      }

      public void setInfo(String info) {
         this.info = info;
      }

      public String getInfo() {
         return this.info;
      }

      Profiler.Section start() {
         return this;
      }

      protected Profiler.Section stop() {
         return this;
      }

      public Profiler.Section end() {
         return this;
      }

      public Profiler.Section next(String name) {
         this.end();
         return this;
      }

      void mark() {
      }

      public long getTime() {
         return 0L;
      }

      public long getTotalTime() {
         return 0L;
      }

      public double getSeconds() {
         return 0.0;
      }

      public double getTotalSeconds() {
         return 0.0;
      }

      public long[] getTimes() {
         return new long[1];
      }

      public int getCount() {
         return 0;
      }

      public int getTotalCount() {
         return 0;
      }

      public double getAverageTime() {
         return 0.0;
      }

      public double getTotalAverageTime() {
         return 0.0;
      }

      @Override
      public final String toString() {
         return this.name;
      }

      protected long getMarkedTime() {
         return 0L;
      }

      protected int getMarkedCount() {
         return 0;
      }
   }

   class SubSection extends Profiler.LiveSection {
      private final String baseName;
      private final Profiler.Section root;

      SubSection(String name, int cursor, String baseName, Profiler.Section root) {
         super(name, cursor);
         this.baseName = baseName;
         this.root = root;
      }

      @Override
      Profiler.Section invalidate() {
         this.root.invalidate();
         return super.invalidate();
      }

      @Override
      public String getBaseName() {
         return this.baseName;
      }

      @Override
      public void setInfo(String info) {
         this.root.setInfo(info);
         super.setInfo(info);
      }

      @Override
      Profiler.Section getDelegate() {
         return this.root;
      }

      @Override
      Profiler.Section start() {
         this.root.start();
         return super.start();
      }

      @Override
      public Profiler.Section end() {
         this.root.stop();
         return super.end();
      }

      @Override
      public Profiler.Section next(String name) {
         super.stop();
         return this.root.next(name);
      }
   }
}
