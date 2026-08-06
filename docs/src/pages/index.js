import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useBaseUrl from '@docusaurus/useBaseUrl';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';
import styles from './index.module.css';

const FeatureList = [
	{
		title: 'No Java Required',
		link: '/getting-started/first-bundle',
		description: 'Create mods using Groovy scripts. No compilation, no build tools. Just drop a folder and reload.',
	},
	{
		title: 'Bundle System',
		link: '/core-concepts/what-is-a-bundle',
		description: 'Self-contained mod packaging with scripts, textures, models, and data in one folder.',
	},
	{
		title: 'Builder API',
		link: '/core-concepts/creating-items',
		description: 'Simple builders for items, blocks, recipes, sounds, and particles with callback hooks.',
	},
	{
		title: 'Event System',
		link: '/core-concepts/events-guide',
		description: 'React to block breaks, player joins, mob deaths, and 80+ other game events.',
	},
	{
		title: 'In-Game Editor',
		link: '/systems/editor',
		description: 'Dockable editor with panels for cutscenes, areas, waypoints, file browsing, and more.',
	},
	{
		title: 'Cutscene System',
		link: '/systems/cutscenes',
		description: 'Bezier camera paths with keyframe commands, screen effects, and in-world editor.',
	},
	{
		title: 'Custom Worlds',
		link: '/systems/instanced-worlds',
		description: 'Create runtime dimensions with custom chunk generators, game rules, and difficulty.',
	},
	{
		title: 'Game Stages',
		link: '/systems/stages',
		description: 'Gate items, mobs, dimensions, and recipes behind progression milestones.',
	},
	{
		title: 'Areas & Waypoints',
		link: '/systems/areas',
		description: 'Spatial zones with enter/leave events and colored in-world markers.',
	},
	{
		title: 'Custom Particles',
		link: '/systems/particles',
		description: 'Keyframe-driven particle system with color, scale, velocity, and rotation over time.',
	},
	{
		title: 'Post-Processing',
		link: '/systems/post-processing',
		description: 'Built-in shader effects (grayscale, sepia, bloom, blur) with fade transitions.',
	},
	{
		title: 'Mesh Rendering',
		link: '/systems/mesh-rendering',
		description: 'Custom 3D rendering pipeline with OBJ model support and engine scene depth.',
	},
	{
		title: 'Dialogue System',
		link: '/systems/dialogue',
		description: 'Branching NPC conversations with conditions, actions, and display modes.',
	},
	{
		title: 'Game Sessions',
		link: '/systems/game-sessions',
		description: 'Stateful lifecycle system for minigames and custom game modes with persistent data.',
	},
	{
		title: 'Java Addon API',
		link: '/advanced/addon-api',
		description: 'Public events and builders for Java developers to extend the engine.',
	},
];

function Feature({title, link, description}) {
	return (
			<div className={clsx('col col--4')}>
				<Link to={link} className={styles.featureLink}>
					<div className="text--center padding-horiz--md">
						<Heading as="h3">{title}</Heading>
						<p>{description}</p>
					</div>
				</Link>
			</div>
	);
}

function HomepageHeader() {
	const {siteConfig} = useDocusaurusContext();
	return (
			<header className={clsx('hero', styles.heroBanner)}>
				<div className="container">
					<img src={useBaseUrl('logo_transparent.png')} alt="FoundryEngine Logo" className={styles.heroLogo}/>
					<Heading as="h1" className={clsx('hero__title', styles.heroTitle)}>
						{siteConfig.title}
					</Heading>
					<p className={clsx('hero__subtitle', styles.heroSubtitle)}>
						Turn Minecraft into a Game Engine
					</p>
					<p className={styles.heroTagline}>{siteConfig.tagline}</p>
					<div className={styles.buttons}>
						<Link className="button button--primary button--lg" to="/getting-started/">
							Get Started
						</Link>
						<Link className="button button--secondary button--lg" to="/getting-started/installation">
							Installation
						</Link>
						<Link className="button button--secondary button--lg" href="https://github.com/LuckyMcDev/FoundryEngine">
							View on GitHub
						</Link>
					</div>
				</div>
			</header>
	);
}

export default function Home() {
	const {siteConfig} = useDocusaurusContext();
	return (
			<Layout title={siteConfig.title} description={siteConfig.tagline}>
				<HomepageHeader/>
				<main>
					<section className={styles.features}>
						<div className="container">
							<div className="row">
								{FeatureList.map((props, idx) => (
										<Feature key={idx} {...props} />
								))}
							</div>
						</div>
					</section>
				</main>
			</Layout>
	);
}