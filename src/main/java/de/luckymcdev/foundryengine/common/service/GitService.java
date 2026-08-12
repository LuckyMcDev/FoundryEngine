package de.luckymcdev.foundryengine.common.service;

import java.util.concurrent.CompletableFuture;

public interface GitService extends EngineService {

	CompletableFuture<EngineServiceResult> init(String directory);

	CompletableFuture<EngineServiceResult> clone(String url, String directory);

	CompletableFuture<EngineServiceResult> cloneDepth(String url, String directory, int depth);

	CompletableFuture<EngineServiceResult> status();

	CompletableFuture<EngineServiceResult> diff();

	CompletableFuture<EngineServiceResult> diffStaged();

	CompletableFuture<EngineServiceResult> add(String... paths);

	CompletableFuture<EngineServiceResult> addAll();

	CompletableFuture<EngineServiceResult> restore(String... paths);

	CompletableFuture<EngineServiceResult> restoreStaged(String... paths);

	CompletableFuture<EngineServiceResult> commit(String message);

	CompletableFuture<EngineServiceResult> commitAmend(String message);

	CompletableFuture<EngineServiceResult> commitAmendNoEdit();

	CompletableFuture<EngineServiceResult> branch();

	CompletableFuture<EngineServiceResult> branchAll();

	CompletableFuture<EngineServiceResult> branchCreate(String name);

	CompletableFuture<EngineServiceResult> branchDelete(String name);

	CompletableFuture<EngineServiceResult> branchDeleteForce(String name);

	CompletableFuture<EngineServiceResult> checkout(String branch);

	CompletableFuture<EngineServiceResult> checkoutNew(String branch);

	CompletableFuture<EngineServiceResult> checkoutFile(String branch, String path);

	CompletableFuture<EngineServiceResult> fetch();

	CompletableFuture<EngineServiceResult> fetchAll();

	CompletableFuture<EngineServiceResult> fetchPrune();

	CompletableFuture<EngineServiceResult> pull();

	CompletableFuture<EngineServiceResult> pullRebase();

	CompletableFuture<EngineServiceResult> push();

	CompletableFuture<EngineServiceResult> pushForce();

	CompletableFuture<EngineServiceResult> pushTags();

	CompletableFuture<EngineServiceResult> pushSetUpstream(String remote, String branch);

	CompletableFuture<EngineServiceResult> remoteList();

	CompletableFuture<EngineServiceResult> remoteAdd(String name, String url);

	CompletableFuture<EngineServiceResult> remoteRemove(String name);

	CompletableFuture<EngineServiceResult> remoteSetUrl(String name, String url);

	CompletableFuture<EngineServiceResult> merge(String branch);

	CompletableFuture<EngineServiceResult> mergeAbort();

	CompletableFuture<EngineServiceResult> rebase(String branch);

	CompletableFuture<EngineServiceResult> rebaseAbort();

	CompletableFuture<EngineServiceResult> rebaseContinue();

	CompletableFuture<EngineServiceResult> cherryPick(String commit);

	CompletableFuture<EngineServiceResult> stash();

	CompletableFuture<EngineServiceResult> stashPush(String message);

	CompletableFuture<EngineServiceResult> stashPop();

	CompletableFuture<EngineServiceResult> stashApply(int index);

	CompletableFuture<EngineServiceResult> stashList();

	CompletableFuture<EngineServiceResult> stashDrop(int index);

	CompletableFuture<EngineServiceResult> tagList();

	CompletableFuture<EngineServiceResult> tagCreate(String name);

	CompletableFuture<EngineServiceResult> tagCreateAnnotated(String name, String message);

	CompletableFuture<EngineServiceResult> tagCreateAt(String name, String commit);

	CompletableFuture<EngineServiceResult> tagDelete(String name);

	CompletableFuture<EngineServiceResult> submoduleAdd(String url, String path);

	CompletableFuture<EngineServiceResult> submoduleUpdate();

	CompletableFuture<EngineServiceResult> submoduleUpdateInit();

	CompletableFuture<EngineServiceResult> submoduleUpdateRecursive();

	CompletableFuture<EngineServiceResult> submoduleStatus();

	CompletableFuture<EngineServiceResult> submoduleSync();

	CompletableFuture<EngineServiceResult> submoduleDeinit(String path);

	CompletableFuture<EngineServiceResult> log(int limit);

	CompletableFuture<EngineServiceResult> logOneline(int limit);

	CompletableFuture<EngineServiceResult> logGraph(int limit);

	CompletableFuture<EngineServiceResult> show(String commit);

	CompletableFuture<EngineServiceResult> blame(String file);

	CompletableFuture<EngineServiceResult> currentBranch();

	CompletableFuture<EngineServiceResult> currentCommit();

	CompletableFuture<EngineServiceResult> describeTag();

	CompletableFuture<EngineServiceResult> clean(boolean force, boolean directories);

	CompletableFuture<EngineServiceResult> reset(String commit);

	CompletableFuture<EngineServiceResult> resetHard(String commit);

	CompletableFuture<EngineServiceResult> resetSoft(String commit);

	CompletableFuture<EngineServiceResult> configGet(String key);

	CompletableFuture<EngineServiceResult> configSet(String key, String value);

	CompletableFuture<EngineServiceResult> configSetGlobal(String key, String value);

	class Default extends CliEngineService implements GitService {

		public Default() {
			super("git");
		}

		@Override
		public String name() {
			return "git";
		}

		@Override
		public CompletableFuture<EngineServiceResult> init(String directory) {
			return execute("init", directory);
		}

		@Override
		public CompletableFuture<EngineServiceResult> clone(String url, String directory) {
			return execute("clone", url, directory);
		}

		@Override
		public CompletableFuture<EngineServiceResult> cloneDepth(String url, String directory, int depth) {
			return execute("clone", "--depth", String.valueOf(depth), url, directory);
		}

		@Override
		public CompletableFuture<EngineServiceResult> status() {
			return execute("status", "--short");
		}

		@Override
		public CompletableFuture<EngineServiceResult> diff() {
			return execute("diff");
		}

		@Override
		public CompletableFuture<EngineServiceResult> diffStaged() {
			return execute("diff", "--staged");
		}

		@Override
		public CompletableFuture<EngineServiceResult> add(String... paths) {
			String[] args = new String[paths.length + 1];
			args[0] = "add";
			System.arraycopy(paths, 0, args, 1, paths.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> addAll() {
			return execute("add", "--all");
		}

		@Override
		public CompletableFuture<EngineServiceResult> restore(String... paths) {
			String[] args = new String[paths.length + 1];
			args[0] = "restore";
			System.arraycopy(paths, 0, args, 1, paths.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> restoreStaged(String... paths) {
			String[] args = new String[paths.length + 2];
			args[0] = "restore";
			args[1] = "--staged";
			System.arraycopy(paths, 0, args, 2, paths.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> commit(String message) {
			return execute("commit", "-m", message);
		}

		@Override
		public CompletableFuture<EngineServiceResult> commitAmend(String message) {
			return execute("commit", "--amend", "-m", message);
		}

		@Override
		public CompletableFuture<EngineServiceResult> commitAmendNoEdit() {
			return execute("commit", "--amend", "--no-edit");
		}

		@Override
		public CompletableFuture<EngineServiceResult> branch() {
			return execute("branch");
		}

		@Override
		public CompletableFuture<EngineServiceResult> branchAll() {
			return execute("branch", "-a");
		}

		@Override
		public CompletableFuture<EngineServiceResult> branchCreate(String name) {
			return execute("branch", name);
		}

		@Override
		public CompletableFuture<EngineServiceResult> branchDelete(String name) {
			return execute("branch", "-d", name);
		}

		@Override
		public CompletableFuture<EngineServiceResult> branchDeleteForce(String name) {
			return execute("branch", "-D", name);
		}

		@Override
		public CompletableFuture<EngineServiceResult> checkout(String branch) {
			return execute("checkout", branch);
		}

		@Override
		public CompletableFuture<EngineServiceResult> checkoutNew(String branch) {
			return execute("checkout", "-b", branch);
		}

		@Override
		public CompletableFuture<EngineServiceResult> checkoutFile(String branch, String path) {
			return execute("checkout", branch, "--", path);
		}

		@Override
		public CompletableFuture<EngineServiceResult> fetch() {
			return execute("fetch");
		}

		@Override
		public CompletableFuture<EngineServiceResult> fetchAll() {
			return execute("fetch", "--all");
		}

		@Override
		public CompletableFuture<EngineServiceResult> fetchPrune() {
			return execute("fetch", "--prune");
		}

		@Override
		public CompletableFuture<EngineServiceResult> pull() {
			return execute("pull");
		}

		@Override
		public CompletableFuture<EngineServiceResult> pullRebase() {
			return execute("pull", "--rebase");
		}

		@Override
		public CompletableFuture<EngineServiceResult> push() {
			return execute("push");
		}

		@Override
		public CompletableFuture<EngineServiceResult> pushForce() {
			return execute("push", "--force-with-lease");
		}

		@Override
		public CompletableFuture<EngineServiceResult> pushTags() {
			return execute("push", "--tags");
		}

		@Override
		public CompletableFuture<EngineServiceResult> pushSetUpstream(String remote, String branch) {
			return execute("push", "--set-upstream", remote, branch);
		}

		@Override
		public CompletableFuture<EngineServiceResult> remoteList() {
			return execute("remote", "-v");
		}

		@Override
		public CompletableFuture<EngineServiceResult> remoteAdd(String name, String url) {
			return execute("remote", "add", name, url);
		}

		@Override
		public CompletableFuture<EngineServiceResult> remoteRemove(String name) {
			return execute("remote", "remove", name);
		}

		@Override
		public CompletableFuture<EngineServiceResult> remoteSetUrl(String name, String url) {
			return execute("remote", "set-url", name, url);
		}

		@Override
		public CompletableFuture<EngineServiceResult> merge(String branch) {
			return execute("merge", branch);
		}

		@Override
		public CompletableFuture<EngineServiceResult> mergeAbort() {
			return execute("merge", "--abort");
		}

		@Override
		public CompletableFuture<EngineServiceResult> rebase(String branch) {
			return execute("rebase", branch);
		}

		@Override
		public CompletableFuture<EngineServiceResult> rebaseAbort() {
			return execute("rebase", "--abort");
		}

		@Override
		public CompletableFuture<EngineServiceResult> rebaseContinue() {
			return execute("rebase", "--continue");
		}

		@Override
		public CompletableFuture<EngineServiceResult> cherryPick(String commit) {
			return execute("cherry-pick", commit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> stash() {
			return execute("stash");
		}

		@Override
		public CompletableFuture<EngineServiceResult> stashPush(String message) {
			return execute("stash", "push", "-m", message);
		}

		@Override
		public CompletableFuture<EngineServiceResult> stashPop() {
			return execute("stash", "pop");
		}

		@Override
		public CompletableFuture<EngineServiceResult> stashApply(int index) {
			return execute("stash", "apply", "stash@{" + index + "}");
		}

		@Override
		public CompletableFuture<EngineServiceResult> stashList() {
			return execute("stash", "list");
		}

		@Override
		public CompletableFuture<EngineServiceResult> stashDrop(int index) {
			return execute("stash", "drop", "stash@{" + index + "}");
		}

		@Override
		public CompletableFuture<EngineServiceResult> tagList() {
			return execute("tag", "-l");
		}

		@Override
		public CompletableFuture<EngineServiceResult> tagCreate(String name) {
			return execute("tag", name);
		}

		@Override
		public CompletableFuture<EngineServiceResult> tagCreateAnnotated(String name, String message) {
			return execute("tag", "-a", name, "-m", message);
		}

		@Override
		public CompletableFuture<EngineServiceResult> tagCreateAt(String name, String commit) {
			return execute("tag", name, commit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> tagDelete(String name) {
			return execute("tag", "-d", name);
		}

		@Override
		public CompletableFuture<EngineServiceResult> submoduleAdd(String url, String path) {
			return execute("submodule", "add", url, path);
		}

		@Override
		public CompletableFuture<EngineServiceResult> submoduleUpdate() {
			return execute("submodule", "update");
		}

		@Override
		public CompletableFuture<EngineServiceResult> submoduleUpdateInit() {
			return execute("submodule", "update", "--init");
		}

		@Override
		public CompletableFuture<EngineServiceResult> submoduleUpdateRecursive() {
			return execute("submodule", "update", "--init", "--recursive");
		}

		@Override
		public CompletableFuture<EngineServiceResult> submoduleStatus() {
			return execute("submodule", "status");
		}

		@Override
		public CompletableFuture<EngineServiceResult> submoduleSync() {
			return execute("submodule", "sync");
		}

		@Override
		public CompletableFuture<EngineServiceResult> submoduleDeinit(String path) {
			return execute("submodule", "deinit", path);
		}

		@Override
		public CompletableFuture<EngineServiceResult> log(int limit) {
			return execute("log", "-" + limit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> logOneline(int limit) {
			return execute("log", "--oneline", "-" + limit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> logGraph(int limit) {
			return execute("log", "--oneline", "--graph", "--decorate", "-" + limit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> show(String commit) {
			return execute("show", commit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> blame(String file) {
			return execute("blame", file);
		}

		@Override
		public CompletableFuture<EngineServiceResult> currentBranch() {
			return execute("rev-parse", "--abbrev-ref", "HEAD");
		}

		@Override
		public CompletableFuture<EngineServiceResult> currentCommit() {
			return execute("rev-parse", "HEAD");
		}

		@Override
		public CompletableFuture<EngineServiceResult> describeTag() {
			return execute("describe", "--tags", "--always");
		}

		@Override
		public CompletableFuture<EngineServiceResult> clean(boolean force, boolean directories) {
			String flags = "-" + (force ? "f" : "") + (directories ? "d" : "");
			return execute("clean", flags);
		}

		@Override
		public CompletableFuture<EngineServiceResult> reset(String commit) {
			return execute("reset", commit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> resetHard(String commit) {
			return execute("reset", "--hard", commit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> resetSoft(String commit) {
			return execute("reset", "--soft", commit);
		}

		@Override
		public CompletableFuture<EngineServiceResult> configGet(String key) {
			return execute("config", "--get", key);
		}

		@Override
		public CompletableFuture<EngineServiceResult> configSet(String key, String value) {
			return execute("config", key, value);
		}

		@Override
		public CompletableFuture<EngineServiceResult> configSetGlobal(String key, String value) {
			return execute("config", "--global", key, value);
		}
	}
}