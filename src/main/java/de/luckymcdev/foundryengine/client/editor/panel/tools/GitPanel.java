package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.imgui.text.ImGuiCoreTextEditor;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorTheme;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.service.EngineServiceResult;
import de.luckymcdev.foundryengine.common.service.GitService;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GitPanel extends EditorPanel {
	public static final GitPanel INSTANCE = new GitPanel();

	private static final int TAB_STATUS = 0;
	private static final int TAB_DIFF = 1;
	private static final int TAB_LOG = 2;
	private static final int TAB_BRANCH = 3;
	private static final int TAB_REMOTE = 4;
	private static final int TAB_STASH = 5;
	private static final int TAB_TAGS = 6;
	// diff viewer
	private final ImGuiCoreTextEditor diffViewer;
	// plain output (non-diff tabs)
	private final List<OutputLine> output = new ArrayList<>();
	// shared ref input (branch name, commit hash, etc.)
	private final ImString refInput = new ImString(128);
	private final ImString commitMessage = new ImString(256);
	// remote tab
	private final ImString remoteName = new ImString(64);
	private final ImString remoteUrl = new ImString(256);
	private final ImString pushRemote = new ImString(64);
	private final ImString pushBranch = new ImString(128);
	// stash tab
	private final ImString stashMessage = new ImString(128);
	// tag tab
	private final ImString tagName = new ImString(64);
	private final ImString tagMessage = new ImString(256);
	private final ImString tagCommit = new ImString(64);
	private int activeTab = TAB_STATUS;
	private boolean running = false;
	private final ImInt stashIndex = new ImInt(0);
	// status tab
	private String currentBranch = "";
	private String currentCommit = "";
	// log tab
	private int logLimit = 20;

	private GitPanel() {
		super(new Builder(Common.id("git"))
			.icon(ImIcons.CODE_BRANCH)
			.category(PanelCategory.TOOLS)
			.menuBar(true));

		EditorTheme theme = EditorTheme.monokai().build();
		this.diffViewer = ImGuiCoreTextEditor.createForLanguage(ImGuiCoreTextEditor.Language.DIFF, theme);
		this.diffViewer.setReadOnly(true);
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		Optional<GitService> git = Common.getEngineServiceManager().get(GitService.class);

		if (git.isEmpty()) {
			g.centeredMessage(ImIcons.EXCLAMATION_TRIANGLE + "  git is not available on this system.");
			return;
		}

		renderMenuBar(git.get());

		switch (activeTab) {
			case TAB_STATUS -> renderStatusTab(g, git.get());
			case TAB_DIFF -> renderDiffTab(g, git.get());
			case TAB_LOG -> renderLogTab(g, git.get());
			case TAB_BRANCH -> renderBranchTab(g, git.get());
			case TAB_REMOTE -> renderRemoteTab(g, git.get());
			case TAB_STASH -> renderStashTab(g, git.get());
			case TAB_TAGS -> renderTagsTab(g, git.get());
		}

		// Output panel only on non-diff tabs
		if (activeTab != TAB_DIFF) {
			renderOutput(g);
		}
	}

	// -------------------------------------------------------------------------
	// Menu bar
	// -------------------------------------------------------------------------

	private void renderMenuBar(GitService git) {
		menuBar(() -> {
			if (ImGui.menuItem(ImIcons.REFRESH + " Refresh")) {
				refreshStatus(git);
			}

			ImGui.separator();

			String[] tabs = {"Status", "Diff", "Log", "Branch", "Remote", "Stash", "Tags"};
			for (int i = 0; i < tabs.length; i++) {
				boolean selected = activeTab == i;
				if (selected) {
					ImGui.pushStyleColor(ImGuiCol.Text, 0xFF4CAF50);
				}
				if (ImGui.menuItem(tabs[i])) {
					activeTab = i;
				}
				if (selected) {
					ImGui.popStyleColor();
				}
			}

			ImGui.separator();

			if (activeTab == TAB_DIFF) {
				if (ImGui.menuItem(ImIcons.COPY + " Copy")) {
					ImGui.setClipboardText(diffViewer.getText());
				}
			} else {
				if (ImGui.menuItem(ImIcons.TRASH + " Clear Output")) {
					output.clear();
				}
			}
		});
	}

	// -------------------------------------------------------------------------
	// Status tab
	// -------------------------------------------------------------------------

	private void renderStatusTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_info_card");
		ImGui.text(ImIcons.CODE_BRANCH + "  Branch:  ");
		ImGui.sameLine();
		ImGui.textColored(0xFF4CAF50, currentBranch.isEmpty() ? "—" : currentBranch);
		ImGui.text(ImIcons.TAG + "  Commit:  ");
		ImGui.sameLine();
		ImGui.textDisabled(currentCommit.isEmpty() ? "—" : currentCommit);
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_status_actions");
		ImGui.text(ImIcons.FOLDER_OPEN + "  Working Tree");
		ImGui.spacing();

		if (button(ImIcons.LIST + " Status")) {
			run(git.status());
		}
		ImGui.sameLine();
		if (button(ImIcons.PLUS + " Add All")) {
			run(git.addAll());
		}

		ImGui.spacing();
		ImGui.text(ImIcons.PENCIL + "  Commit");
		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##commit_msg", "Commit message...", commitMessage, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.CHECK + " Commit")) {
			if (!commitMessage.isEmpty()) {
				String msg = commitMessage.get();
				run(git.addAll().thenCompose(r -> git.commit(msg)));
				commitMessage.set("");
			}
		}

		ImGui.spacing();
		if (button(ImIcons.UNDO + " Amend (no edit)")) {
			run(git.commitAmendNoEdit());
		}
		g.helpTooltip("Amend the last commit without changing its message.");
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_cleanup_card");
		ImGui.text(ImIcons.BROOM + "  Cleanup");
		ImGui.spacing();
		if (button(ImIcons.TRASH + " Clean (files)")) {
			run(git.clean(true, false));
		}
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Clean (+ dirs)")) {
			run(git.clean(true, true));
		}
		g.cardEnd();

		if (currentBranch.isEmpty()) {
			refreshStatus(git);
		}
	}

	// -------------------------------------------------------------------------
	// Diff tab — uses the DiffColorizer-backed text editor
	// -------------------------------------------------------------------------

	private void renderDiffTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_diff_controls");
		ImGui.text(ImIcons.SEARCH + "  Diff");
		ImGui.spacing();

		if (button(ImIcons.SEARCH + " Working Tree")) {
			runDiff(git.diff());
		}
		ImGui.sameLine();
		if (button(ImIcons.SEARCH + " Staged")) {
			runDiff(git.diffStaged());
		}

		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##diff_ref", "Commit / branch / ref...", refInput, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.SEARCH + " Show")) {
			if (!refInput.isEmpty()) {
				runDiff(git.show(refInput.get()));
			}
		}
		g.helpTooltip("Show a specific commit or ref as a diff.");

		g.cardEnd();

		ImGui.spacing();

		if (running) {
			ImGui.textDisabled(ImIcons.SPINNER + "  Fetching diff...");
			return;
		}

		float availH = ImGui.getContentRegionAvailY();
		diffViewer.render("##diff_view", ImGui.getContentRegionAvailX(), availH, false);
	}

	// -------------------------------------------------------------------------
	// Log tab
	// -------------------------------------------------------------------------

	private void renderLogTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_log_card");
		ImGui.text(ImIcons.HISTORY + "  Commit History");
		ImGui.spacing();

		ImGui.text("Limit: ");
		ImGui.sameLine();
		ImGui.setNextItemWidth(80);
		int[] limitRef = {logLimit};
		if (ImGui.sliderInt("##log_limit", limitRef, 5, 100)) {
			logLimit = limitRef[0];
		}

		ImGui.spacing();

		if (button(ImIcons.LIST + " Log")) {
			run(git.log(logLimit));
		}
		ImGui.sameLine();
		if (button(ImIcons.LIST + " Oneline")) {
			run(git.logOneline(logLimit));
		}
		ImGui.sameLine();
		if (button(ImIcons.SITEMAP + " Graph")) {
			run(git.logGraph(logLimit));
		}
		ImGui.sameLine();
		if (button(ImIcons.TAG + " Describe Tag")) {
			run(git.describeTag());
		}

		ImGui.spacing();
		ImGui.separator();
		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##show_ref", "Commit hash or ref...", refInput, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.EYE + " Show in Diff")) {
			if (!refInput.isEmpty()) {
				activeTab = TAB_DIFF;
				runDiff(git.show(refInput.get()));
			}
		}
		g.helpTooltip("Opens the commit in the Diff viewer.");

		g.cardEnd();
	}

	// -------------------------------------------------------------------------
	// Branch tab
	// -------------------------------------------------------------------------

	private void renderBranchTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_branch_list_card");
		ImGui.text(ImIcons.CODE_BRANCH + "  Branches");
		ImGui.spacing();
		if (button(ImIcons.LIST + " Local")) {
			run(git.branch());
		}
		ImGui.sameLine();
		if (button(ImIcons.GLOBE + " All (incl. remote)")) {
			run(git.branchAll());
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_branch_ops_card");
		ImGui.text(ImIcons.PLUS + "  Create / Checkout");
		ImGui.spacing();

		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##branch_ref", "Branch name...", refInput, ImGuiInputTextFlags.None);
		ImGui.spacing();

		if (button(ImIcons.PLUS + " Create")) {
			if (!refInput.isEmpty()) {
				run(git.branchCreate(refInput.get()));
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.SIGN_OUT + " Checkout")) {
			if (!refInput.isEmpty()) {
				run(git.checkout(refInput.get()));
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.SIGN_OUT + " Create & Checkout")) {
			if (!refInput.isEmpty()) {
				run(git.checkoutNew(refInput.get()));
			}
		}

		ImGui.spacing();

		if (button(ImIcons.TRASH + " Delete (safe)")) {
			if (!refInput.isEmpty()) {
				run(git.branchDelete(refInput.get()));
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.EXCLAMATION_TRIANGLE + " Force Delete")) {
			if (!refInput.isEmpty()) {
				run(git.branchDeleteForce(refInput.get()));
			}
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_merge_card");
		ImGui.text(ImIcons.CODE_BRANCH + "  Merge / Rebase");
		ImGui.spacing();

		if (button(ImIcons.CODE_BRANCH + " Merge")) {
			if (!refInput.isEmpty()) {
				run(git.merge(refInput.get()));
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.TIMES + " Abort Merge")) {
			run(git.mergeAbort());
		}

		ImGui.spacing();

		if (button(ImIcons.EXCHANGE + " Rebase")) {
			if (!refInput.isEmpty()) {
				run(git.rebase(refInput.get()));
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.TIMES + " Abort Rebase")) {
			run(git.rebaseAbort());
		}
		ImGui.sameLine();
		if (button(ImIcons.CHECK + " Continue Rebase")) {
			run(git.rebaseContinue());
		}
		g.cardEnd();
	}

	// -------------------------------------------------------------------------
	// Remote tab
	// -------------------------------------------------------------------------

	private void renderRemoteTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_sync_card");
		ImGui.text(ImIcons.CLOUD + "  Sync");
		ImGui.spacing();

		if (button(ImIcons.DOWNLOAD + " Pull")) {
			run(git.pull());
		}
		ImGui.sameLine();
		if (button(ImIcons.EXCHANGE + " Pull --rebase")) {
			run(git.pullRebase());
		}
		ImGui.sameLine();
		if (button(ImIcons.UPLOAD + " Push")) {
			run(git.push());
		}
		ImGui.sameLine();
		if (button(ImIcons.EXCLAMATION_TRIANGLE + " Force Push")) {
			run(git.pushForce());
		}
		g.helpTooltip("Uses --force-with-lease.");

		ImGui.spacing();

		if (button(ImIcons.REFRESH + " Fetch")) {
			run(git.fetch());
		}
		ImGui.sameLine();
		if (button(ImIcons.REFRESH + " Fetch All")) {
			run(git.fetchAll());
		}
		ImGui.sameLine();
		if (button(ImIcons.REFRESH + " Fetch + Prune")) {
			run(git.fetchPrune());
		}
		ImGui.sameLine();
		if (button(ImIcons.UPLOAD + " Push Tags")) {
			run(git.pushTags());
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_upstream_card");
		ImGui.text(ImIcons.SIGN_OUT + "  Set Upstream");
		ImGui.spacing();

		ImGui.setNextItemWidth(120);
		ImGui.inputTextWithHint("##push_remote", "Remote (origin)", pushRemote, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##push_branch", "Branch (main)", pushBranch, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.UPLOAD + " Push")) {
			String r = pushRemote.isEmpty() ? "origin" : pushRemote.get();
			String b = pushBranch.isEmpty() ? "main" : pushBranch.get();
			run(git.pushSetUpstream(r, b));
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_remotes_card");
		ImGui.text(ImIcons.SERVER + "  Remotes");
		ImGui.spacing();

		if (button(ImIcons.LIST + " List Remotes")) {
			run(git.remoteList());
		}

		ImGui.spacing();

		ImGui.setNextItemWidth(120);
		ImGui.inputTextWithHint("##remote_name", "Name (origin)", remoteName, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##remote_url", "URL...", remoteUrl, ImGuiInputTextFlags.None);
		ImGui.sameLine();

		if (button(ImIcons.PLUS + " Add")) {
			if (!remoteName.isEmpty() && !remoteUrl.isEmpty()) {
				run(git.remoteAdd(remoteName.get(), remoteUrl.get()));
			}
		}

		ImGui.spacing();

		if (button(ImIcons.PENCIL + " Set URL")) {
			if (!remoteName.isEmpty() && !remoteUrl.isEmpty()) {
				run(git.remoteSetUrl(remoteName.get(), remoteUrl.get()));
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Remove")) {
			if (!remoteName.isEmpty()) {
				run(git.remoteRemove(remoteName.get()));
			}
		}
		g.cardEnd();
	}
	
	private void renderStashTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_stash_card");
		ImGui.text(ImIcons.ARCHIVE + "  Stash");
		ImGui.spacing();

		if (button(ImIcons.LIST + " List")) {
			run(git.stashList());
		}

		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##stash_msg", "Stash message (optional)...", stashMessage, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.INBOX + " Stash")) {
			if (!stashMessage.isEmpty()) {
				String msg = stashMessage.get();
				run(git.stashPush(msg));
				stashMessage.set("");
			}
		}

		ImGui.spacing();

		ImGui.text("Index: ");
		ImGui.sameLine();
		ImGui.setNextItemWidth(80);
		if (ImGui.inputInt("##stash_index", stashIndex)) {
			stashIndex.set(Math.max(0, stashIndex.get()));
		}

		ImGui.spacing();

		if (button(ImIcons.SIGN_OUT + " Pop")) {
			run(git.stashPop());
		}
		ImGui.sameLine();
		if (button(ImIcons.SIGN_OUT + " Apply [n]")) {
			run(git.stashApply(stashIndex.get()));
		}
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Drop [n]")) {
			run(git.stashDrop(stashIndex.get()));
		}
		g.cardEnd();
	}

	private void renderTagsTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_tags_card");
		ImGui.text(ImIcons.TAG + "  Tags");
		ImGui.spacing();

		if (button(ImIcons.LIST + " List Tags")) {
			run(git.tagList());
		}
		ImGui.sameLine();
		if (button(ImIcons.TAG + " Describe")) {
			run(git.describeTag());
		}

		ImGui.spacing();
		ImGui.separator();
		ImGui.spacing();

		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##tag_name", "Tag name (e.g. v1.2.0)...", tagName, ImGuiInputTextFlags.None);
		ImGui.spacing();
		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##tag_msg", "Annotated tag message (optional)...", tagMessage, ImGuiInputTextFlags.None);
		ImGui.spacing();
		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##tag_commit", "Commit (leave empty for HEAD)...", tagCommit, ImGuiInputTextFlags.None);
		ImGui.spacing();

		if (button(ImIcons.PLUS + " Lightweight")) {
			if (!tagName.isEmpty()) {
				if (tagCommit.isEmpty()) {
					run(git.tagCreate(tagName.get()));
				} else {
					run(git.tagCreateAt(tagName.get(), tagCommit.get()));
				}
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.PLUS + " Annotated")) {
			if (!tagName.isEmpty() && !tagMessage.isEmpty()) {
				run(git.tagCreateAnnotated(tagName.get(), tagMessage.get()));
			}
		}
		g.helpTooltip("Annotated tags store author, date, and message — recommended for mod releases.");
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Delete")) {
			if (!tagName.isEmpty()) {
				run(git.tagDelete(tagName.get()));
			}
		}

		ImGui.spacing();
		if (button(ImIcons.UPLOAD + " Push Tags to Remote")) {
			run(git.pushTags());
		}
		g.cardEnd();
	}

	private void renderOutput(ImGraphicsExtractor g) {
		ImGui.spacing();
		ImGui.separator();

		g.section("Output");

		if (running) {
			ImGui.textDisabled(ImIcons.SPINNER + "  Running...");
		}

		float availH = ImGui.getContentRegionAvailY();
		if (availH < 60) {
			availH = 120;
		}

		g.scrollableRegion("##git_output", 0, availH, false, () -> {
			for (OutputLine line : output) {
				if (line.error()) {
					ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.33f, 0.33f, 1.0f);
					ImGui.textUnformatted(line.text());
					ImGui.popStyleColor();
				} else {
					ImGui.textUnformatted(line.text());
				}
			}
			ImGui.setScrollHereY(1.0f);
		});
	}

	private void refreshStatus(GitService git) {
		git.currentBranch().thenAccept(r -> {
			if (r.success()) {
				currentBranch = r.stdout();
			}
		});
		git.currentCommit().thenAccept(r -> {
			if (r.success()) {
				currentCommit = r.stdout().length() > 8 ? r.stdout().substring(0, 8) : r.stdout();
			}
		});
	}

	/**
	 * Run a command whose output goes into the diff viewer. Switches to the diff tab.
	 */
	private void runDiff(CompletableFuture<EngineServiceResult> future) {
		running = true;
		activeTab = TAB_DIFF;
		diffViewer.setText("Loading...");
		future.thenAccept(result -> {
			running = false;
			String content = result.stdout().isEmpty() ? result.stderr() : result.stdout();
			if (content.isEmpty()) {
				content = result.success() ? "(empty diff)" : "Failed (exit " + result.exitCode() + ")";
			}
			diffViewer.setText(content);
		});
	}

	/**
	 * Run a command whose output goes into the plain output log.
	 */
	private void run(CompletableFuture<EngineServiceResult> future) {
		running = true;
		future.thenAccept(result -> {
			running = false;
			if (!result.stdout().isEmpty()) {
				for (String line : result.stdout().split("\n")) {
					output.add(new OutputLine(line, false));
				}
			}
			if (!result.stderr().isEmpty()) {
				for (String line : result.stderr().split("\n")) {
					output.add(new OutputLine(line, true));
				}
			}
			if (result.stdout().isEmpty() && result.stderr().isEmpty()) {
				output.add(new OutputLine(result.success() ? "Done." : "Failed (exit " + result.exitCode() + ").", !result.success()));
			}
		});
	}

	private boolean button(String label) {
		return !running && ImGui.button(label);
	}

	private record OutputLine(String text, boolean error) {
	}
}